package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.SanPham;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.KichThuocRepository;
import org.example.yourchoiceshop.repository.MauSacRepository;
import org.example.yourchoiceshop.repository.SanPhamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class ChatAiServicePriceQueryTest {

    @Mock
    private SanPhamRepository sanPhamRepository;

    @Mock
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Mock
    private MauSacRepository mauSacRepository;

    @Mock
    private KichThuocRepository kichThuocRepository;

    @Mock
    private ChatKnowledgeService chatKnowledgeService;

    @InjectMocks
    private ChatAiService chatAiService;

    @BeforeEach
    void setup() {
        lenient().when(chatKnowledgeService.findBestMatch(anyString(), anyList())).thenReturn(Optional.empty());
        lenient().when(chatKnowledgeService.suggestMatches(anyString(), anyList(), anyInt())).thenReturn(Collections.emptyList());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("priceFilterQueries")
    void should_parse_price_queries_and_search_products(String query, Long expectedMin, Long expectedMax) {
        when(chiTietSanPhamRepository.aiSmartSearch(any(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)))
            .thenReturn(List.of(sampleVariant()));

        clearInvocations(chiTietSanPhamRepository);

        Map<String, Object> result = chatAiService.processMessage(query, Collections.emptyList());

        ArgumentCaptor<BigDecimal> minCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> maxCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(chiTietSanPhamRepository, atLeastOnce()).aiSmartSearch(
                any(), any(), any(), minCaptor.capture(), maxCaptor.capture(), anyBoolean(), any(Pageable.class));

        BigDecimal actualMin = minCaptor.getAllValues().isEmpty() ? null : minCaptor.getAllValues().get(0);
        BigDecimal actualMax = maxCaptor.getAllValues().isEmpty() ? null : maxCaptor.getAllValues().get(0);

        assertMoney(expectedMin, actualMin);
        assertMoney(expectedMax, actualMax);

        assertNotNull(result.get("products"));
        assertTrue(result.get("products") instanceof List<?>);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("priceIntentWithoutNumbers")
    void should_ask_for_price_when_price_intent_has_no_number(String query) {
        clearInvocations(chiTietSanPhamRepository);

        Map<String, Object> result = chatAiService.processMessage(query, Collections.emptyList());

        verifyNoInteractions(chiTietSanPhamRepository);

        assertNotNull(result.get("reply"));
        assertTrue(result.get("reply").toString().contains("Bạn muốn lọc theo mức giá nào"));
        assertEquals(Collections.emptyList(), result.get("products"));
    }

    private static Stream<Arguments> priceFilterQueries() {
        return Stream.of(
                // Dưới giá
                Arguments.of("cho tôi sản phẩm dưới 500k", null, 500_000L),
                Arguments.of("cho tôi sản phẩm dưới 500 nghìn", null, 500_000L),
                Arguments.of("dưới mức giá 900000", null, 900_000L),
                Arguments.of("under 300k", null, 300_000L),
                Arguments.of("không quá 1 triệu", null, 1_000_000L),
                Arguments.of("max 450k", null, 450_000L),
                Arguments.of("nhỏ hơn 350k", null, 350_000L),
                Arguments.of("ít hơn 1.2tr", null, 1_200_000L),
                Arguments.of("cho tôi hàng 500k trở xuống", null, 500_000L),
                Arguments.of("sản phẩm 1.200.000 trở xuống", null, 1_200_000L),

                // Trên giá
                Arguments.of("cho tôi sản phẩm trên 300k", 300_000L, null),
                Arguments.of("trên mức giá 900000", 900_000L, null),
                Arguments.of("từ 500k trở lên", 500_000L, null),
                Arguments.of("tối thiểu 700k", 700_000L, null),
                Arguments.of("min 1tr", 1_000_000L, null),
                Arguments.of("lớn hơn 450k", 450_000L, null),
                Arguments.of("cao hơn 1.200.000", 1_200_000L, null),
                Arguments.of("900k trở lên", 900_000L, null),

                // Khoảng giá
                Arguments.of("từ 300k đến 700k", 300_000L, 700_000L),
                Arguments.of("trong khoảng giá 200k - 500k", 200_000L, 500_000L),
                Arguments.of("khoảng giá 250k tới 650k", 250_000L, 650_000L),
                Arguments.of("mức giá 300000 den 800000", 300_000L, 800_000L),
                Arguments.of("500k đến 900k", 500_000L, 900_000L),
                Arguments.of("từ 1 triệu đến 1.5 triệu", 1_000_000L, 1_500_000L),
                Arguments.of("từ 1,2tr đến 2tr", 1_200_000L, 2_000_000L),
                Arguments.of("1.200.000 đến 1.800.000", 1_200_000L, 1_800_000L),
                Arguments.of("từ 900k đến 500k", 500_000L, 900_000L),
                Arguments.of("trong khoảng 700000 - 1200000", 700_000L, 1_200_000L),
                Arguments.of("mức giá 1tr - 1,6tr", 1_000_000L, 1_600_000L),
                Arguments.of("cho tôi áo polo từ 300k tới 600k", 300_000L, 600_000L),
                Arguments.of("cho tôi sản phẩm từ 450000 đến 550000", 450_000L, 550_000L),
                Arguments.of("tìm quần jean khoảng 1,5 triệu đến 2 triệu", 1_500_000L, 2_000_000L),

                // Tầm giá (biên độ theo nghiệp vụ mới)
                Arguments.of("cho tôi sản phẩm tầm 500k", 400_000L, 650_000L),
                Arguments.of("áo thun khoảng 350k", 250_000L, 500_000L),
                Arguments.of("quần jean quanh 900k", 750_000L, 1_150_000L),
                Arguments.of("tầm giá 1.500.000", 1_250_000L, 1_900_000L),
                Arguments.of("xấp xỉ 250k", 180_000L, 350_000L),
                Arguments.of("around 700k", 600_000L, 850_000L),
                Arguments.of("khoảng 70k", 50_000L, 170_000L),
                Arguments.of("tầm 1,2tr", 1_050_000L, 1_450_000L)
        );
    }

    private static Stream<Arguments> priceIntentWithoutNumbers() {
        return Stream.of(
                Arguments.of("cho tôi sản phẩm trong khoảng giá"),
                Arguments.of("cho tôi sản phẩm dưới"),
                Arguments.of("cho tôi sản phẩm trên"),
                Arguments.of("lọc theo giá"),
                Arguments.of("tầm giá bao nhiêu cũng được"),
                Arguments.of("sản phẩm giá hợp lý"),
                Arguments.of("muốn tìm đồ theo giá"),
                Arguments.of("khoảng giá đẹp"),
                Arguments.of("mức giá"),
                Arguments.of("budget")
        );
    }

    private static void assertMoney(Long expected, BigDecimal actual) {
        if (expected == null) {
            assertEquals(null, actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual));
    }

    private ChiTietSanPham sampleVariant() {
        SanPham sp = new SanPham();
        sp.setId(1);
        sp.setMaSanPham("SP-TEST-01");
        sp.setTenSanPham("Ao Polo Test");

        ChiTietSanPham ctsp = new ChiTietSanPham();
        ctsp.setMaCtsp("CTSP-TEST-01");
        ctsp.setSanPham(sp);
        ctsp.setGiaBan(BigDecimal.valueOf(450_000));
        ctsp.setSoLuong(20);
        return ctsp;
    }
}
