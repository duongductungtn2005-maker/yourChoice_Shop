package org.example.yourchoiceshop.service;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.ChatKnowledgeRequest;
import org.example.yourchoiceshop.dto.response.ChatKnowledgeResponse;
import org.example.yourchoiceshop.entity.ChatKnowledge;
import org.example.yourchoiceshop.repository.ChatKnowledgeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatKnowledgeService {

    private static final Set<String> STOP_WORDS = Set.of(
            "la", "là", "va", "và", "cho", "minh", "mình", "toi", "tôi", "shop",
            "co", "có", "khong", "không", "duoc", "được", "nhe", "nhé", "a", "ạ", "oi", "ơi"
    );

    private final ChatKnowledgeRepository chatKnowledgeRepository;

    public List<ChatKnowledgeResponse> getAll() {
        return chatKnowledgeRepository.findAllByOrderByDoUuTienDescIdDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ChatKnowledgeResponse create(ChatKnowledgeRequest request) {
        ChatKnowledge k = new ChatKnowledge();
        applyRequest(k, request);
        return toResponse(chatKnowledgeRepository.save(k));
    }

    @Transactional
    public ChatKnowledgeResponse update(Integer id, ChatKnowledgeRequest request) {
        ChatKnowledge k = chatKnowledgeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tri thức chat"));
        applyRequest(k, request);
        return toResponse(chatKnowledgeRepository.save(k));
    }

    @Transactional
    public void deactivate(Integer id) {
        ChatKnowledge k = chatKnowledgeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tri thức chat"));
        k.setTrangThai(false);
        chatKnowledgeRepository.save(k);
    }

    @Transactional
    public void markUsed(Integer id) {
        chatKnowledgeRepository.findById(id).ifPresent(k -> {
            k.setSoLanSuDung((k.getSoLanSuDung() == null ? 0 : k.getSoLanSuDung()) + 1);
            k.setLanSuDungCuoi(LocalDateTime.now());
            chatKnowledgeRepository.save(k);
        });
    }

    public Optional<KnowledgeMatch> findBestMatch(String userMessage, List<String> contextMessages) {
        if (userMessage == null || userMessage.isBlank()) return Optional.empty();

        List<KnowledgeMatch> matches = scoreMatches(userMessage, contextMessages, 50);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    public List<KnowledgeMatch> suggestMatches(String userMessage, List<String> contextMessages, int limit) {
        if (limit <= 0) return Collections.emptyList();
        List<KnowledgeMatch> matches = scoreMatches(userMessage, contextMessages, 50);
        return matches.stream().limit(limit).collect(Collectors.toList());
    }

    private List<KnowledgeMatch> scoreMatches(String userMessage, List<String> contextMessages, int maxCandidates) {
        String normalizedQuery = normalize(userMessage);
        Set<String> queryTokens = tokenize(normalizedQuery);
        Set<String> contextTokens = tokenize(normalize(String.join(" ", contextMessages == null ? Collections.emptyList() : contextMessages)));

        List<ChatKnowledge> candidates = chatKnowledgeRepository.searchCandidates(normalizedQuery, PageRequest.of(0, maxCandidates));
        if (candidates.isEmpty()) {
            candidates = chatKnowledgeRepository.findByTrangThaiTrueOrderByDoUuTienDescIdDesc()
                    .stream().limit(maxCandidates).collect(Collectors.toList());
        }

        List<KnowledgeMatch> matches = new ArrayList<>();
        for (ChatKnowledge k : candidates) {
            double score = score(k, normalizedQuery, queryTokens, contextTokens);
            if (score >= 0.25d) {
                matches.add(new KnowledgeMatch(k, score));
            }
        }

        matches.sort(Comparator.comparingDouble(KnowledgeMatch::score).reversed()
                .thenComparing(m -> m.knowledge().getDoUuTien(), Comparator.nullsLast(Comparator.reverseOrder())));
        return matches;
    }

    private double score(ChatKnowledge k, String normalizedQuery, Set<String> queryTokens, Set<String> contextTokens) {
        String normalizedQuestion = normalize(k.getCauHoiMau());
        Set<String> questionTokens = tokenize(normalizedQuestion);

        double questionScore = jaccard(queryTokens, questionTokens);

        List<String> keywordList = parseKeywords(k.getTuKhoa());
        long keywordHits = keywordList.stream().filter(normalizedQuery::contains).count();
        double keywordScore = keywordList.isEmpty() ? 0d : (double) keywordHits / keywordList.size();

        double exactPhraseBoost = normalizedQuestion.contains(normalizedQuery) || normalizedQuery.contains(normalizedQuestion) ? 0.20d : 0d;

        long contextHits = keywordList.stream().filter(contextTokens::contains).count();
        double contextBoost = keywordList.isEmpty() ? 0d : Math.min(0.10d, (double) contextHits / keywordList.size() * 0.10d);

        int priority = k.getDoUuTien() == null ? 0 : k.getDoUuTien();
        double priorityBoost = Math.min(0.08d, priority * 0.01d);

        double total = questionScore * 0.55d + keywordScore * 0.30d + exactPhraseBoost + contextBoost + priorityBoost;
        return Math.min(1d, total);
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0d;
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        if (intersection.isEmpty()) return 0d;
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }

    private List<String> parseKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) return Collections.emptyList();
        return Arrays.stream(keywords.split(","))
                .map(this::normalize)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        return Arrays.stream(text.split("\\s+"))
                .map(String::trim)
                .filter(s -> s.length() > 1)
                .filter(s -> !STOP_WORDS.contains(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalize(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase(Locale.ROOT).trim();
        String noAccent = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccent.replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void applyRequest(ChatKnowledge k, ChatKnowledgeRequest request) {
        k.setCauHoiMau(request.getCauHoiMau());
        k.setTuKhoa(request.getTuKhoa());
        k.setCauTraLoi(request.getCauTraLoi());
        k.setDoUuTien(request.getDoUuTien() == null ? 0 : request.getDoUuTien());
        k.setTrangThai(request.getTrangThai() == null ? true : request.getTrangThai());
    }

    private ChatKnowledgeResponse toResponse(ChatKnowledge k) {
        ChatKnowledgeResponse r = new ChatKnowledgeResponse();
        r.setId(k.getId());
        r.setCauHoiMau(k.getCauHoiMau());
        r.setTuKhoa(k.getTuKhoa());
        r.setCauTraLoi(k.getCauTraLoi());
        r.setDoUuTien(k.getDoUuTien());
        r.setTrangThai(k.getTrangThai());
        r.setSoLanSuDung(k.getSoLanSuDung());
        return r;
    }

    public record KnowledgeMatch(ChatKnowledge knowledge, double score) {
    }
}
