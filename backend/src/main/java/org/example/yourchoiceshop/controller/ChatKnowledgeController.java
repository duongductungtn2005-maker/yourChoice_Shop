package org.example.yourchoiceshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.ChatKnowledgeRequest;
import org.example.yourchoiceshop.dto.response.ChatKnowledgeResponse;
import org.example.yourchoiceshop.service.ChatKnowledgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/knowledge")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatKnowledgeController {

    private final ChatKnowledgeService chatKnowledgeService;

    @GetMapping
    public ResponseEntity<List<ChatKnowledgeResponse>> getAll() {
        return ResponseEntity.ok(chatKnowledgeService.getAll());
    }

    @PostMapping
    public ResponseEntity<ChatKnowledgeResponse> create(@Valid @RequestBody ChatKnowledgeRequest request) {
        return ResponseEntity.ok(chatKnowledgeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatKnowledgeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody ChatKnowledgeRequest request
    ) {
        return ResponseEntity.ok(chatKnowledgeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        chatKnowledgeService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
