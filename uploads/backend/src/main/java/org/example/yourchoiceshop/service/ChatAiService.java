package org.example.yourchoiceshop.service;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.entity.*;
import org.example.yourchoiceshop.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatAiService {

    private final SanPhamRepository sanPhamRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;

    // ===== Từ điển màu sắc tiếng Việt → tên trong DB =====
    private static final Map<String, List<String>> COLOR_ALIASES = new LinkedHashMap<>();
    static {
        COLOR_ALIASES.put("đỏ", List.of("đỏ", "red"));
        COLOR_ALIASES.put("xanh dương", List.of("xanh dương", "blue", "xanh biển", "navy"));
        COLOR_ALIASES.put("xanh lá", List.of("xanh lá", "green", "xanh lục"));
        COLOR_ALIASES.put("xanh", List.of("xanh"));
        COLOR_ALIASES.put("trắng", List.of("trắng", "white"));
        COLOR_ALIASES.put("đen", List.of("đen", "black"));
        COLOR_ALIASES.put("vàng", List.of("vàng", "yellow"));
        COLOR_ALIASES.put("hồng", List.of("hồng", "pink"));
        COLOR_ALIASES.put("tím", List.of("tím", "purple"));
        COLOR_ALIASES.put("cam", List.of("cam", "orange"));
        COLOR_ALIASES.put("nâu", List.of("nâu", "brown"));
        COLOR_ALIASES.put("xám", List.of("xám", "grey", "gray"));
        COLOR_ALIASES.put("bạc", List.of("bạc", "silver"));
        COLOR_ALIASES.put("kem", List.of("kem", "be", "beige"));
        COLOR_ALIASES.put("navy", List.of("navy", "xanh navy"));
        COLOR_ALIASES.put("rêu", List.of("rêu", "xanh rêu"));
        COLOR_ALIASES.put("ghi", List.of("ghi"));
    }

    // ===== Từ điển size =====
    private static final String[] SIZE_NAMES = {"S", "M", "L", "XL", "XXL", "XXXL", "2XL", "3XL", "4XL",
            "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44"};

    // ===== Từ khóa sản phẩm =====
    private static final String[] PRODUCT_KEYWORDS = {
            "áo polo", "áo thun", "áo sơ mi", "áo hoodie", "áo khoác", "áo vest", "áo blazer",
            "áo len", "áo nỉ", "áo dài tay", "áo ngắn tay", "áo ba lỗ",
            "quần jean", "quần kaki", "quần short", "quần dài", "quần jogger", "quần tây",
            "quần âu", "quần bò", "quần đùi",
            "giày", "giày sneaker", "giày boot", "giày thể thao", "giày da",
            "dép", "mũ", "nón", "polo", "hoodie", "jacket", "jean", "kaki", "short",
            "sơ mi", "thun", "vest", "blazer", "sneaker", "boot",
            "áo", "quần"
    };

    // ===== Từ điển chất liệu =====
    private static final Map<String, List<String>> MATERIAL_ALIASES = new LinkedHashMap<>();
    static {
        MATERIAL_ALIASES.put("cotton", List.of("cotton", "bông"));
        MATERIAL_ALIASES.put("polyester", List.of("polyester", "poly"));
        MATERIAL_ALIASES.put("kaki", List.of("kaki", "khaki"));
        MATERIAL_ALIASES.put("jean", List.of("jean", "denim", "bò"));
        MATERIAL_ALIASES.put("lụa", List.of("lụa", "silk"));
        MATERIAL_ALIASES.put("len", List.of("len", "wool"));
        MATERIAL_ALIASES.put("nỉ", List.of("nỉ", "fleece"));
        MATERIAL_ALIASES.put("linen", List.of("linen", "lanh"));
        MATERIAL_ALIASES.put("vải dù", List.of("vải dù", "dù", "nylon"));
        MATERIAL_ALIASES.put("thun", List.of("thun", "spandex", "co giãn"));
    }

    // ==========================================================================================
    // PUBLIC API
    // ==========================================================================================

    public Map<String, Object> processMessage(String userMessage) {
        Map<String, Object> result = new HashMap<>();
        String msg = userMessage.toLowerCase().trim();

        // 1. Lời chào
        if (isGreeting(msg)) {
            result.put("reply", "Chào bạn! 👋 Mình là trợ lý AI của YourChoice Shop. Mình có thể giúp bạn:\n" +
                    "• 🔍 Tìm sản phẩm (theo tên, màu sắc, size, chất liệu, giá...)\n" +
                    "• 📏 Tư vấn size (cho mình chiều cao, cân nặng)\n" +
                    "• 🏷️ Xem sản phẩm đang giảm giá\n" +
                    "• 📦 Kiểm tra tồn kho\n" +
                    "• 👔 Gợi ý phối đồ\n" +
                    "• ❓ Hỏi về giao hàng, đổi trả, thanh toán\n\n" +
                    "Bạn cần hỗ trợ gì ạ? 😊");
            result.put("products", Collections.emptyList());
            return result;
        }

        // 2. Chuyển nhân viên
        if (wantsStaffTransfer(msg)) {
            result.put("reply", "Bạn muốn nói chuyện với nhân viên tư vấn? Mình sẽ chuyển phiên chat này cho nhân viên hỗ trợ nhé. Vui lòng chờ trong giây lát! 👨‍💼");
            result.put("products", Collections.emptyList());
            result.put("transferToStaff", true);
            return result;
        }

        // 3. Tư vấn size
        if (isSizeConsultation(msg)) {
            return handleSizeConsultation(msg);
        }

        // 4. Hỏi sản phẩm giảm giá / khuyến mãi
        if (isDiscountQuery(msg)) {
            return handleDiscountQuery(msg);
        }

        // 5. Kiểm tra tồn kho cụ thể
        if (isStockQuery(msg)) {
            return handleStockQuery(msg);
        }

        // 6. Gợi ý phối đồ
        if (isOutfitQuery(msg)) {
            return handleOutfitSuggestion(msg);
        }

        // 7. FAQ - Chăm sóc quần áo
        if (isCareQuery(msg)) {
            return handleCareQuery(msg);
        }

        // 8. FAQ chung
        Map<String, Object> faqResult = handleFAQ(msg);
        if (faqResult != null) return faqResult;

        // 9. Tìm sản phẩm thông minh (mặc định cho mọi yêu cầu có keyword/thuộc tính)
        Map<String, Object> searchResult = handleSmartProductSearch(msg);
        if (searchResult != null) return searchResult;

        // 10. Mặc định
        result.put("reply", "Mình có thể giúp bạn:\n" +
                "• 🔍 Tìm sản phẩm: \"áo polo trắng size L\", \"quần jean dưới 500k\"\n" +
                "• 🎨 Tìm theo màu: \"áo màu đen\", \"quần xanh navy\"\n" +
                "• 📏 Tư vấn size: \"mình cao 170 nặng 65\"\n" +
                "• 🏷️ Xem giảm giá: \"sản phẩm đang sale\", \"có gì giảm giá không\"\n" +
                "• 📦 Kiểm kho: \"áo polo còn hàng không\"\n" +
                "• 👔 Phối đồ: \"phối đồ với quần jean\", \"mặc gì đi chơi\"\n" +
                "• ❓ FAQ: giao hàng, đổi trả, thanh toán, bảo quản\n\n" +
                "Bạn cứ hỏi tự nhiên nhé! 😊");
        result.put("products", Collections.emptyList());
        return result;
    }

    // ==========================================================================================
    // INTENT DETECTION
    // ==========================================================================================

    private boolean isGreeting(String msg) {
        String[] greetings = {"xin chào", "chào shop", "chào bạn", "chào", "hello", "hi ", "hey", "alo",
                "cho mình hỏi", "shop ơi", "mình cần hỏi", "bot ơi", "ai ơi"};
        for (String g : greetings) {
            if (msg.startsWith(g) || msg.equals(g.trim())) return true;
        }
        return msg.length() <= 5 && (msg.equals("hi") || msg.equals("hey"));
    }

    private boolean wantsStaffTransfer(String msg) {
        String[] keywords = {"nhân viên", "tư vấn viên", "người thật", "nói chuyện với người",
                "gặp nhân viên", "kết nối nhân viên", "chuyển nhân viên", "cần tư vấn trực tiếp"};
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }

    private boolean isSizeConsultation(String msg) {
        return (msg.contains("cao") && msg.contains("nặng"))
                || (msg.contains("size") && (msg.contains("cao") || msg.contains("nặng") || msg.contains("cm") || msg.contains("kg")))
                || msg.contains("tư vấn size") || msg.contains("chọn size") || msg.contains("nên mặc size")
                || msg.matches(".*\\d{2,3}\\s*(cm|kg).*\\d{2,3}\\s*(cm|kg).*");
    }

    private boolean isDiscountQuery(String msg) {
        String[] keywords = {"giảm giá", "khuyến mãi", "sale", "đang giảm", "khuyến mại",
                "ưu đãi", "deal", "flash sale", "sản phẩm giảm", "đồ giảm giá",
                "có gì giảm", "đang sale", "hàng sale"};
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }

    private boolean isStockQuery(String msg) {
        String[] keywords = {"còn hàng", "hết hàng", "tồn kho", "còn không", "còn size",
                "còn màu", "stock", "có sẵn", "có hàng không", "bao nhiêu cái"};
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }

    private boolean isOutfitQuery(String msg) {
        String[] keywords = {"phối đồ", "mix đồ", "mặc với", "kết hợp với", "mix match",
                "outfit", "mặc gì", "phối với", "đi chơi mặc gì", "đi làm mặc gì",
                "dạo phố mặc gì", "hẹn hò mặc gì", "gợi ý outfit", "phong cách"};
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }

    private boolean isCareQuery(String msg) {
        String[] keywords = {"bảo quản", "giặt", "ủi", "là đồ", "chăm sóc", "giữ form",
                "phai màu", "co rút", "bảo dưỡng", "phơi", "sấy"};
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }

    // ==========================================================================================
    // HANDLER: Tìm sản phẩm thông minh
    // ==========================================================================================

    private Map<String, Object> handleSmartProductSearch(String msg) {
        // Trích xuất tất cả thuộc tính từ tin nhắn
        String colorName = extractColor(msg);
        String sizeName = extractSize(msg);
        String keyword = extractProductKeyword(msg);
        BigDecimal minPrice = extractMinPrice(msg);
        BigDecimal maxPrice = extractMaxPrice(msg);
        boolean onlyDiscount = msg.contains("giảm giá") || msg.contains("sale") || msg.contains("khuyến mãi");

        // Nếu không trích xuất được gì → return null để xử lý mặc định
        if (keyword == null && colorName == null && sizeName == null
                && minPrice == null && maxPrice == null && !onlyDiscount) {
            return null;
        }

        Pageable pageable = PageRequest.of(0, 15);
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.aiSmartSearch(
                keyword, colorName, sizeName, minPrice, maxPrice, onlyDiscount, pageable);

        if (variants.isEmpty()) {
            // Thử tìm mở rộng: bỏ bớt filter
            variants = chiTietSanPhamRepository.aiSmartSearch(
                    keyword, null, null, minPrice, maxPrice, false, pageable);
        }

        if (variants.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            StringBuilder sb = new StringBuilder("Xin lỗi, mình không tìm thấy sản phẩm");
            if (keyword != null) sb.append(" \"").append(keyword).append("\"");
            if (colorName != null) sb.append(" màu ").append(colorName);
            if (sizeName != null) sb.append(" size ").append(sizeName);
            if (maxPrice != null) sb.append(" giá dưới ").append(formatMoney(maxPrice));
            sb.append(". 🔍\n\nBạn thử:\n• Mô tả lại sản phẩm cần tìm\n• Bớt điều kiện lọc\n• Hoặc gõ \"xem giảm giá\" để xem sản phẩm đang sale");
            result.put("reply", sb.toString());
            result.put("products", Collections.emptyList());
            return result;
        }

        // Nhóm theo sản phẩm (SanPham) để hiện gọn
        List<Map<String, Object>> productList = groupVariantsByProduct(variants);

        Map<String, Object> result = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("Mình tìm thấy ").append(productList.size()).append(" sản phẩm");
        if (keyword != null) sb.append(" \"").append(keyword).append("\"");
        if (colorName != null) sb.append(" màu ").append(colorName);
        if (sizeName != null) sb.append(" size ").append(sizeName.toUpperCase());
        if (maxPrice != null) sb.append(" dưới ").append(formatMoney(maxPrice));
        if (minPrice != null) sb.append(" trên ").append(formatMoney(minPrice));
        if (onlyDiscount) sb.append(" đang giảm giá 🏷️");
        sb.append(":\n\n");

        for (int i = 0; i < Math.min(productList.size(), 5); i++) {
            Map<String, Object> p = productList.get(i);
            sb.append("🛍️ ").append(p.get("tenSanPham"));
            if (p.get("thuongHieu") != null) sb.append(" - ").append(p.get("thuongHieu"));
            sb.append("\n");

            // Hiện giá
            BigDecimal giaMin = (BigDecimal) p.get("giaMin");
            BigDecimal giaMax = (BigDecimal) p.get("giaMax");
            sb.append("   💰 ").append(formatMoney(giaMin));
            if (giaMin != null && giaMax != null && giaMin.compareTo(giaMax) != 0) {
                sb.append(" ~ ").append(formatMoney(giaMax));
            }

            // Hiện giảm giá nếu có
            if (p.get("giamGia") != null) {
                sb.append("  🏷️ -").append(p.get("giamGia")).append("%");
            }
            sb.append("\n");

            // Hiện màu sắc và size có sẵn
            @SuppressWarnings("unchecked")
            List<String> colors = (List<String>) p.get("mauSacs");
            @SuppressWarnings("unchecked")
            List<String> sizes = (List<String>) p.get("kichThuocs");
            if (colors != null && !colors.isEmpty()) {
                sb.append("   🎨 Màu: ").append(String.join(", ", colors)).append("\n");
            }
            if (sizes != null && !sizes.isEmpty()) {
                sb.append("   📏 Size: ").append(String.join(", ", sizes)).append("\n");
            }
            sb.append("   📦 Tồn kho: ").append(p.get("tonKho")).append(" sản phẩm\n\n");
        }
        sb.append("Bấm vào sản phẩm để xem chi tiết nhé! 👆");
        result.put("reply", sb.toString());
        result.put("products", productList);
        return result;
    }

    // ==========================================================================================
    // HANDLER: Tư vấn size
    // ==========================================================================================

    private Map<String, Object> handleSizeConsultation(String msg) {
        Map<String, Object> result = new HashMap<>();
        Integer height = extractNumber(msg, "cao", "cm");
        Integer weight = extractNumber(msg, "nặng", "kg");

        if (height == null) height = extractNumber(msg, "\\d{3}", null); // 3-digit number likely height
        if (weight == null) weight = extractNumber(msg, "\\d{2}", null); // 2-digit number likely weight

        // Extract height/weight from patterns like "170cm 65kg" or "1m70 65kg"
        if (height == null || weight == null) {
            Matcher m = Pattern.compile("(\\d{2,3})\\s*(cm)?[\\s,/]+(\\d{2,3})\\s*(kg)?").matcher(msg);
            if (m.find()) {
                int n1 = Integer.parseInt(m.group(1));
                int n2 = Integer.parseInt(m.group(3));
                if (n1 > 100) { height = n1; weight = n2; }
                else if (n2 > 100) { height = n2; weight = n1; }
                else { height = n1 > n2 ? n1 : null; weight = n1 <= n2 ? n1 : n2; }
            }
        }

        // "1m70" pattern
        if (height == null) {
            Matcher m = Pattern.compile("(1)\\s*m\\s*(\\d{1,2})").matcher(msg);
            if (m.find()) {
                height = 100 + Integer.parseInt(m.group(2).length() == 1 ? m.group(2) + "0" : m.group(2));
            }
        }

        if (height == null && weight == null) {
            result.put("reply", "Để tư vấn size chính xác, bạn cho mình biết:\n" +
                    "• 📏 Chiều cao (cm)\n• ⚖️ Cân nặng (kg)\n\n" +
                    "Ví dụ: \"Mình cao 170cm nặng 65kg\" 😊");
            result.put("products", Collections.emptyList());
            return result;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📏 Tư vấn size cho bạn");
        if (height != null) sb.append(" (cao ").append(height).append("cm");
        if (weight != null) sb.append(", nặng ").append(weight).append("kg");
        if (height != null || weight != null) sb.append(")");
        sb.append(":\n\n");

        // Tư vấn size áo
        String shirtSize = recommendShirtSize(height, weight);
        sb.append("👕 **Size áo:** ").append(shirtSize).append("\n");

        // Tư vấn size quần
        String pantSize = recommendPantSize(height, weight);
        sb.append("👖 **Size quần:** ").append(pantSize).append("\n\n");

        // Bảng size tham khảo
        sb.append("📊 Bảng size tham khảo:\n");
        sb.append("┌─────┬──────────┬──────────┐\n");
        sb.append("│ Size │ Cao (cm) │ Nặng (kg)│\n");
        sb.append("├─────┼──────────┼──────────┤\n");
        sb.append("│  S  │ 155-163  │  45-55   │\n");
        sb.append("│  M  │ 163-170  │  55-63   │\n");
        sb.append("│  L  │ 168-175  │  63-73   │\n");
        sb.append("│  XL │ 173-182  │  73-83   │\n");
        sb.append("│ XXL │ 178-188  │  83-95   │\n");
        sb.append("└─────┴──────────┴──────────┘\n\n");

        sb.append("💡 Lưu ý: Bảng size chỉ mang tính tham khảo. Nếu bạn thích mặc rộng (oversize), nên chọn lên 1 size.\n\n");
        sb.append("Bạn muốn mình tìm sản phẩm size ").append(shirtSize.split(" ")[0]).append(" không? 😊");

        result.put("reply", sb.toString());
        result.put("products", Collections.emptyList());

        // Tự động tìm sản phẩm phù hợp size
        String keyword = extractProductKeyword(msg);
        if (keyword != null) {
            String sizeRec = shirtSize.split(" ")[0]; // Lấy size chính (e.g., "M")
            Pageable pageable = PageRequest.of(0, 10);
            List<ChiTietSanPham> variants = chiTietSanPhamRepository.aiSmartSearch(
                    keyword, null, sizeRec, null, null, false, pageable);
            if (!variants.isEmpty()) {
                List<Map<String, Object>> productList = groupVariantsByProduct(variants);
                result.put("products", productList);
                sb.append("\n\nMình cũng tìm thấy ").append(productList.size())
                        .append(" sản phẩm \"").append(keyword).append("\" có size ").append(sizeRec).append(":");
                result.put("reply", sb.toString());
            }
        }

        return result;
    }

    private String recommendShirtSize(Integer height, Integer weight) {
        if (height == null) height = 170;
        if (weight == null) weight = 65;

        if (height <= 163 && weight <= 55) return "S (nhỏ)";
        if (height <= 170 && weight <= 63) return "M (vừa)";
        if (height <= 175 && weight <= 73) return "L (lớn)";
        if (height <= 182 && weight <= 83) return "XL (rất lớn)";
        if (height <= 188 && weight <= 95) return "XXL";
        return "XXXL hoặc 3XL";
    }

    private String recommendPantSize(Integer height, Integer weight) {
        if (height == null) height = 170;
        if (weight == null) weight = 65;

        if (weight <= 50) return "28-29";
        if (weight <= 58) return "29-30";
        if (weight <= 65) return "30-31";
        if (weight <= 73) return "31-32";
        if (weight <= 82) return "32-33";
        if (weight <= 90) return "34-35";
        return "36+";
    }

    // ==========================================================================================
    // HANDLER: Sản phẩm giảm giá
    // ==========================================================================================

    private Map<String, Object> handleDiscountQuery(String msg) {
        Map<String, Object> result = new HashMap<>();
        Pageable pageable = PageRequest.of(0, 15);
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.findOnSaleProducts(pageable);

        if (variants.isEmpty()) {
            result.put("reply", "Hiện tại chưa có sản phẩm nào đang giảm giá. Bạn hãy quay lại sau nhé! 🏷️");
            result.put("products", Collections.emptyList());
            return result;
        }

        List<Map<String, Object>> productList = groupVariantsByProduct(variants);

        StringBuilder sb = new StringBuilder();
        sb.append("🏷️ Đang có ").append(productList.size()).append(" sản phẩm giảm giá HOT:\n\n");
        for (int i = 0; i < Math.min(productList.size(), 5); i++) {
            Map<String, Object> p = productList.get(i);
            sb.append("🔥 ").append(p.get("tenSanPham"));
            if (p.get("thuongHieu") != null) sb.append(" - ").append(p.get("thuongHieu"));
            sb.append("\n");
            sb.append("   💰 Giá gốc: ").append(formatMoney((BigDecimal) p.get("giaMin")));
            if (p.get("giaSauGiam") != null) {
                sb.append(" → ").append(formatMoney((BigDecimal) p.get("giaSauGiam")));
            }
            if (p.get("giamGia") != null) {
                sb.append("  (-").append(p.get("giamGia")).append("%)");
            }
            sb.append("\n");
            @SuppressWarnings("unchecked")
            List<String> colors = (List<String>) p.get("mauSacs");
            if (colors != null && !colors.isEmpty()) {
                sb.append("   🎨 Màu: ").append(String.join(", ", colors)).append("\n");
            }
            sb.append("\n");
        }
        sb.append("Bấm vào sản phẩm để mua ngay! 🛒");
        result.put("reply", sb.toString());
        result.put("products", productList);
        return result;
    }

    // ==========================================================================================
    // HANDLER: Kiểm tra tồn kho
    // ==========================================================================================

    private Map<String, Object> handleStockQuery(String msg) {
        String keyword = extractProductKeyword(msg);
        String colorName = extractColor(msg);
        String sizeName = extractSize(msg);

        if (keyword == null && colorName == null && sizeName == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("reply", "Bạn muốn kiểm tra tồn kho sản phẩm nào? Cho mình biết tên sản phẩm nhé! 📦\n" +
                    "Ví dụ: \"Áo polo trắng size M còn hàng không?\"");
            result.put("products", Collections.emptyList());
            return result;
        }

        Pageable pageable = PageRequest.of(0, 10);
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.aiSmartSearch(
                keyword, colorName, sizeName, null, null, false, pageable);

        Map<String, Object> result = new HashMap<>();
        if (variants.isEmpty()) {
            result.put("reply", "Không tìm thấy sản phẩm phù hợp để kiểm tra tồn kho. Bạn thử mô tả lại nhé! 🔍");
            result.put("products", Collections.emptyList());
            return result;
        }

        StringBuilder sb = new StringBuilder("📦 Thông tin tồn kho:\n\n");
        // Hiện chi tiết từng biến thể
        int count = 0;
        for (ChiTietSanPham v : variants) {
            if (count >= 10) break;
            sb.append("• ").append(v.getSanPham() != null ? v.getSanPham().getTenSanPham() : v.getMaCtsp());
            if (v.getMauSac() != null) sb.append(" | Màu: ").append(v.getMauSac().getTenMauSac());
            if (v.getKichThuoc() != null) sb.append(" | Size: ").append(v.getKichThuoc().getTenKichThuoc());
            sb.append("\n  ");
            if (v.getSoLuong() != null && v.getSoLuong() > 0) {
                sb.append("✅ Còn ").append(v.getSoLuong()).append(" sản phẩm");
            } else {
                sb.append("❌ Hết hàng");
            }
            sb.append(" | 💰 ").append(formatMoney(v.getGiaBan()));
            BigDecimal giaSauGiam = v.getGiaSauGiam();
            if (giaSauGiam != null) {
                sb.append(" → ").append(formatMoney(giaSauGiam));
            }
            sb.append("\n\n");
            count++;
        }

        int totalStock = variants.stream()
                .mapToInt(v -> v.getSoLuong() != null ? v.getSoLuong() : 0)
                .sum();
        sb.append("📊 Tổng tồn kho: ").append(totalStock).append(" sản phẩm");

        List<Map<String, Object>> productList = groupVariantsByProduct(variants);
        result.put("reply", sb.toString());
        result.put("products", productList);
        return result;
    }

    // ==========================================================================================
    // HANDLER: Gợi ý phối đồ
    // ==========================================================================================

    private Map<String, Object> handleOutfitSuggestion(String msg) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        // Phân tích ngữ cảnh / dịp
        boolean casual = msg.contains("đi chơi") || msg.contains("dạo phố") || msg.contains("casual") || msg.contains("hàng ngày");
        boolean formal = msg.contains("đi làm") || msg.contains("công sở") || msg.contains("formal") || msg.contains("lịch sự");
        boolean date = msg.contains("hẹn hò") || msg.contains("date");
        boolean sport = msg.contains("thể thao") || msg.contains("sport") || msg.contains("tập gym");

        sb.append("👔 Gợi ý phối đồ");

        if (casual || (!formal && !date && !sport)) {
            sb.append(" phong cách **Casual / Hàng ngày**:\n\n");
            sb.append("1️⃣ **Combo Polo + Quần Kaki:**\n");
            sb.append("   • Áo polo trắng/đen + Quần kaki be/ghi\n");
            sb.append("   • Giày sneaker trắng\n");
            sb.append("   → Lịch sự nhưng thoải mái\n\n");
            sb.append("2️⃣ **Combo Áo Thun + Quần Jean:**\n");
            sb.append("   • Áo thun trơn + Quần jean xanh/đen\n");
            sb.append("   • Giày sneaker hoặc boot\n");
            sb.append("   → Năng động, trẻ trung\n\n");
            sb.append("3️⃣ **Combo Hoodie + Quần Jogger:**\n");
            sb.append("   • Hoodie oversized + Jogger đen\n");
            sb.append("   • Giày thể thao\n");
            sb.append("   → Phong cách streetwear\n");
        }
        if (formal) {
            sb.append(" phong cách **Công sở / Lịch sự**:\n\n");
            sb.append("1️⃣ **Combo Sơ mi + Quần Tây:**\n");
            sb.append("   • Sơ mi trắng/xanh nhạt + Quần tây đen/navy\n");
            sb.append("   • Giày da, thắt lưng cùng tông\n\n");
            sb.append("2️⃣ **Combo Polo + Quần Âu:**\n");
            sb.append("   • Áo polo tối màu + Quần âu ghi/be\n");
            sb.append("   • Giày lười hoặc sneaker tối giản\n");
        }
        if (date) {
            sb.append(" phong cách **Hẹn hò**:\n\n");
            sb.append("1️⃣ Sơ mi trắng + Jean slim fit + Sneaker trắng\n");
            sb.append("2️⃣ Áo polo tối màu + Kaki ôm + Giày da\n");
            sb.append("3️⃣ Áo thun cổ tròn + Blazer + Jean\n");
        }
        if (sport) {
            sb.append(" phong cách **Thể thao**:\n\n");
            sb.append("1️⃣ Áo thun thể thao + Quần jogger\n");
            sb.append("2️⃣ Áo ba lỗ + Short + Giày thể thao\n");
        }

        sb.append("\n\n💡 Bạn muốn mình tìm sản phẩm nào trong gợi ý trên không?");

        // Tìm sản phẩm liên quan
        String keyword = extractProductKeyword(msg);
        List<Map<String, Object>> productList = new ArrayList<>();
        if (keyword != null) {
            Pageable pageable = PageRequest.of(0, 10);
            List<ChiTietSanPham> variants = chiTietSanPhamRepository.aiSmartSearch(
                    keyword, null, null, null, null, false, pageable);
            if (!variants.isEmpty()) {
                productList = groupVariantsByProduct(variants);
            }
        }

        result.put("reply", sb.toString());
        result.put("products", productList);
        return result;
    }

    // ==========================================================================================
    // HANDLER: Chăm sóc quần áo
    // ==========================================================================================

    private Map<String, Object> handleCareQuery(String msg) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder sb = new StringBuilder("🧺 Hướng dẫn bảo quản quần áo:\n\n");

        if (msg.contains("cotton") || msg.contains("thun")) {
            sb.append("**Cotton / Thun:**\n");
            sb.append("• Giặt máy ở chế độ nhẹ, nước lạnh hoặc ấm (≤30°C)\n");
            sb.append("• Lộn trái áo trước khi giặt\n");
            sb.append("• Không sấy nóng (dễ co rút)\n");
            sb.append("• Phơi nơi thoáng mát, tránh ánh nắng trực tiếp\n");
        } else if (msg.contains("jean") || msg.contains("denim") || msg.contains("bò")) {
            sb.append("**Jean / Denim:**\n");
            sb.append("• Lộn trái, giặt nước lạnh\n");
            sb.append("• Hạn chế giặt máy (giặt tay tốt hơn)\n");
            sb.append("• Không dùng thuốc tẩy\n");
            sb.append("• Phơi ngược, tránh nắng gắt để giữ màu\n");
        } else if (msg.contains("lụa") || msg.contains("silk")) {
            sb.append("**Lụa / Silk:**\n");
            sb.append("• Giặt tay nhẹ nhàng với nước lạnh\n");
            sb.append("• Dùng nước giặt chuyên dụng cho lụa\n");
            sb.append("• Phơi nơi thoáng mát, tránh nắng\n");
            sb.append("• Ủi ở nhiệt độ thấp, mặt trái\n");
        } else if (msg.contains("kaki") || msg.contains("khaki")) {
            sb.append("**Kaki / Khaki:**\n");
            sb.append("• Giặt máy, nước ấm 30-40°C\n");
            sb.append("• Phân loại màu trước khi giặt\n");
            sb.append("• Phơi thẳng để giữ form\n");
            sb.append("• Ủi nhiệt độ trung bình\n");
        } else {
            sb.append("**Hướng dẫn chung:**\n");
            sb.append("• 🌡️ Giặt nước lạnh/ấm (≤30°C) để giữ màu và form\n");
            sb.append("• 🔄 Lộn trái trước khi giặt máy\n");
            sb.append("• ☀️ Phơi nơi thoáng mát, tránh nắng trực tiếp\n");
            sb.append("• 🧴 Dùng nước giặt nhẹ, hạn chế tẩy\n");
            sb.append("• 👔 Treo hoặc gấp ngay sau khi phơi khô\n");
            sb.append("• 🔥 Ủi ở nhiệt độ phù hợp với chất liệu\n\n");
            sb.append("Bạn muốn biết cách bảo quản chất liệu cụ thể nào? (cotton, jean, lụa, kaki...)");
        }

        result.put("reply", sb.toString());
        result.put("products", Collections.emptyList());
        return result;
    }

    // ==========================================================================================
    // HANDLER: FAQ
    // ==========================================================================================

    private Map<String, Object> handleFAQ(String msg) {
        Map<String, Object> result = new HashMap<>();

        // Voucher & mã giảm giá
        if (msg.contains("voucher") || msg.contains("phiếu giảm") || msg.contains("mã giảm") || msg.contains("coupon")) {
            result.put("reply", "🎟️ Về voucher/phiếu giảm giá:\n\n" +
                    "• Bạn có thể áp dụng mã giảm giá khi thanh toán tại trang checkout\n" +
                    "• Kiểm tra mục \"Phiếu giảm giá\" trên menu để xem các mã đang có hiệu lực\n" +
                    "• Mỗi đơn hàng chỉ áp dụng được 1 voucher\n" +
                    "• Voucher có thời hạn sử dụng, hãy kiểm tra ngày hết hạn nhé!");
            result.put("products", Collections.emptyList());
            return result;
        }

        // Đổi trả
        if (msg.contains("đổi trả") || msg.contains("hoàn tiền") || msg.contains("trả hàng") || msg.contains("đổi hàng")) {
            result.put("reply", "🔄 Chính sách đổi trả:\n\n" +
                    "• ⏰ Thời gian: 7 ngày kể từ ngày nhận hàng\n" +
                    "• ✅ Điều kiện: Sản phẩm còn nguyên tem mác, chưa qua sử dụng\n" +
                    "• 📋 Quy trình:\n" +
                    "  1. Liên hệ qua chat hoặc hotline\n" +
                    "  2. Cung cấp mã đơn hàng và lý do đổi/trả\n" +
                    "  3. Gửi hàng về theo hướng dẫn\n" +
                    "  4. Nhận hàng mới hoặc hoàn tiền trong 3-5 ngày\n" +
                    "• 💰 Phí ship đổi trả: Miễn phí nếu lỗi từ shop");
            result.put("products", Collections.emptyList());
            return result;
        }

        // Giao hàng & vận chuyển
        if (msg.contains("giao hàng") || msg.contains("vận chuyển") || msg.contains("ship") || msg.contains("phí ship")
                || msg.contains("bao lâu") || msg.contains("mấy ngày")) {
            result.put("reply", "🚚 Thông tin giao hàng:\n\n" +
                    "• 🏢 Đơn vị vận chuyển: GHN Express\n" +
                    "• ⏰ Thời gian giao:\n" +
                    "  - Nội thành HCM/HN: 1-2 ngày\n" +
                    "  - Các tỉnh lân cận: 2-3 ngày\n" +
                    "  - Tỉnh xa: 3-5 ngày\n" +
                    "• 💰 Phí ship: Tính theo khoảng cách và khối lượng (hiện tại bước thanh toán)\n" +
                    "• 🎁 Miễn phí ship cho đơn từ 500.000đ (tùy chương trình)\n" +
                    "• 📦 Theo dõi đơn hàng: Mã vận đơn sẽ được gửi qua SMS/email");
            result.put("products", Collections.emptyList());
            return result;
        }

        // Thanh toán
        if (msg.contains("thanh toán") || msg.contains("trả tiền") || msg.contains("payment") || msg.contains("chuyển khoản")) {
            result.put("reply", "💳 Phương thức thanh toán:\n\n" +
                    "• 💵 COD - Thanh toán khi nhận hàng\n" +
                    "• 🏦 VNPay - Chuyển khoản / Ví điện tử\n" +
                    "• Tất cả các phương thức đều an toàn và bảo mật\n\n" +
                    "Bạn chọn phương thức nào cũng được nhé! 😊");
            result.put("products", Collections.emptyList());
            return result;
        }

        // Đơn hàng
        if (msg.contains("đơn hàng") || msg.contains("theo dõi") || msg.contains("tracking") || msg.contains("kiểm tra đơn")) {
            result.put("reply", "📋 Kiểm tra đơn hàng:\n\n" +
                    "• Đăng nhập vào tài khoản → Mục \"Đơn hàng của tôi\"\n" +
                    "• Bạn có thể xem trạng thái đơn, mã vận đơn tại đây\n" +
                    "• Nếu cần hỗ trợ về đơn hàng cụ thể, hãy cho mình mã đơn hàng nhé!");
            result.put("products", Collections.emptyList());
            return result;
        }

        // Tài khoản
        if (msg.contains("đăng ký") || msg.contains("tạo tài khoản") || msg.contains("đăng nhập") || msg.contains("quên mật khẩu")) {
            result.put("reply", "👤 Hỗ trợ tài khoản:\n\n" +
                    "• 📝 Đăng ký: Nhấn \"Đăng ký\" trên menu, điền thông tin\n" +
                    "• 🔑 Đăng nhập: Sử dụng email/SĐT và mật khẩu\n" +
                    "• 🔄 Quên mật khẩu: Nhấn \"Quên mật khẩu\", nhập email để nhận link đặt lại\n\n" +
                    "Cần hỗ trợ thêm không ạ?");
            result.put("products", Collections.emptyList());
            return result;
        }

        // Cảm ơn
        if (msg.contains("cảm ơn") || msg.contains("thank") || msg.contains("thanks") || msg.equals("ok") || msg.equals("okay")) {
            result.put("reply", "Không có gì ạ! 😊 Nếu cần hỗ trợ thêm, bạn cứ nhắn mình nhé. Chúc bạn mua sắm vui vẻ! 🛍️");
            result.put("products", Collections.emptyList());
            return result;
        }

        return null; // Không khớp FAQ nào
    }

    // ==========================================================================================
    // ENTITY EXTRACTION: Trích xuất thông tin từ tin nhắn
    // ==========================================================================================

    private String extractColor(String msg) {
        // Tìm pattern "màu X" trước
        Matcher m = Pattern.compile("màu\\s+(\\S+(?:\\s+\\S+)?)").matcher(msg);
        if (m.find()) {
            String colorPhrase = m.group(1).toLowerCase().trim();
            // Kiểm tra trong từ điển
            for (Map.Entry<String, List<String>> entry : COLOR_ALIASES.entrySet()) {
                for (String alias : entry.getValue()) {
                    if (colorPhrase.startsWith(alias)) return alias;
                }
            }
            return colorPhrase; // Trả về nguyên nếu không match alias
        }

        // Tìm tên màu trực tiếp trong msg (ưu tiên cụm dài hơn)
        String[] orderedColors = {"xanh dương", "xanh lá", "xanh navy", "xanh rêu",
                "đỏ", "trắng", "đen", "vàng", "hồng", "tím", "cam", "nâu", "xám", "bạc", "kem", "be", "ghi", "navy", "rêu"};
        for (String color : orderedColors) {
            if (msg.contains(color)) return color;
        }

        return null;
    }

    private String extractSize(String msg) {
        // Pattern "size X" hoặc "cỡ X"
        Matcher m = Pattern.compile("(?:size|cỡ|sz)\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE).matcher(msg);
        if (m.find()) {
            return m.group(1).toUpperCase();
        }

        // Tìm size names trực tiếp (ưu tiên dài hơn)
        String[] orderedSizes = {"XXXL", "3XL", "4XL", "XXL", "2XL", "XL", "XS"};
        String upperMsg = msg.toUpperCase();
        for (String sz : orderedSizes) {
            if (upperMsg.contains(sz)) return sz;
        }
        // Single letter sizes - phải có word boundary
        Matcher sm = Pattern.compile("\\b([SMLsml])\\b").matcher(msg);
        if (sm.find()) {
            return sm.group(1).toUpperCase();
        }

        return null;
    }

    private String extractProductKeyword(String msg) {
        // Ưu tiên cụm dài hơn
        for (String kw : PRODUCT_KEYWORDS) {
            if (msg.contains(kw)) return kw;
        }

        // Làm sạch và trích xuất
        String cleaned = msg
                .replaceAll("(?:dưới|trên|từ|đến|khoảng|tầm|giá)\\s*\\d+[.,]?\\d*\\s*(?:k|nghìn|ngàn|triệu|tr|đồng|vnd|vnđ)?", "")
                .replaceAll("(?:màu)\\s+\\S+", "")
                .replaceAll("(?:size|cỡ|sz)\\s*\\S+", "")
                .replaceAll("\\b(?:tìm|kiếm|muốn mua|cho mình|mình muốn|tìm giúp|shop có|bán|giá|bao nhiêu|còn hàng|còn không|có không|mình|cho|có|cái|chiếc|đôi|được không|nào|nhé|ạ|đi|với|và|the|a|is|are)\\b", "")
                .replaceAll("\\b(?:đỏ|xanh|trắng|đen|vàng|hồng|tím|cam|nâu|xám|bạc|kem|be|ghi|navy|rêu)\\b", "")
                .replaceAll("\\s+", " ").trim();

        if (cleaned.length() >= 2) return cleaned;
        return null;
    }

    private BigDecimal extractMaxPrice(String msg) {
        // "dưới 500k", "under 500000", "tối đa 500k"
        Pattern p = Pattern.compile("(?:dưới|under|tối đa|max|không quá)\\s*(\\d+[.,]?\\d*)\\s*(k|nghìn|ngàn|triệu|tr)?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(msg);
        if (m.find()) return parsePrice(m.group(1), m.group(2));

        // "500k trở xuống"
        Pattern p2 = Pattern.compile("(\\d+[.,]?\\d*)\\s*(k|nghìn|ngàn|triệu|tr)\\s*(?:trở xuống|trở lại)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(msg);
        if (m2.find()) return parsePrice(m2.group(1), m2.group(2));

        // "giá từ X đến Y" → Y là max
        Pattern p3 = Pattern.compile("(?:từ|khoảng)\\s*\\d+[.,]?\\d*\\s*(?:k|nghìn|ngàn|triệu|tr)?\\s*(?:đến|tới|-)\\s*(\\d+[.,]?\\d*)\\s*(k|nghìn|ngàn|triệu|tr)?", Pattern.CASE_INSENSITIVE);
        Matcher m3 = p3.matcher(msg);
        if (m3.find()) return parsePrice(m3.group(1), m3.group(2));

        return null;
    }

    private BigDecimal extractMinPrice(String msg) {
        // "trên 200k", "từ 200k"
        Pattern p = Pattern.compile("(?:trên|tối thiểu|min|từ)\\s*(\\d+[.,]?\\d*)\\s*(k|nghìn|ngàn|triệu|tr)?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(msg);
        if (m.find()) {
            // Kiểm tra không phải pattern "từ X đến Y" (X là min)
            return parsePrice(m.group(1), m.group(2));
        }

        // "200k trở lên"
        Pattern p2 = Pattern.compile("(\\d+[.,]?\\d*)\\s*(k|nghìn|ngàn|triệu|tr)\\s*(?:trở lên|trở đi)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(msg);
        if (m2.find()) return parsePrice(m2.group(1), m2.group(2));

        return null;
    }

    private BigDecimal parsePrice(String number, String unit) {
        try {
            BigDecimal value = new BigDecimal(number.replace(",", "."));
            if (unit != null) {
                String u = unit.toLowerCase();
                if (u.equals("k") || u.equals("nghìn") || u.equals("ngàn")) {
                    value = value.multiply(BigDecimal.valueOf(1000));
                } else if (u.equals("triệu") || u.equals("tr")) {
                    value = value.multiply(BigDecimal.valueOf(1000000));
                }
            } else if (value.compareTo(BigDecimal.valueOf(1000)) < 0) {
                // Nếu không có đơn vị và số nhỏ → tự nhân 1000 (e.g., "500" → 500k)
                value = value.multiply(BigDecimal.valueOf(1000));
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer extractNumber(String msg, String beforePattern, String afterPattern) {
        String regex;
        if (afterPattern != null) {
            regex = beforePattern + "\\s*(\\d{2,3})\\s*" + afterPattern + "?";
        } else {
            regex = "(" + beforePattern + ")";
        }
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(msg);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // ==========================================================================================
    // HELPERS
    // ==========================================================================================

    private List<Map<String, Object>> groupVariantsByProduct(List<ChiTietSanPham> variants) {
        // Nhóm ChiTietSanPham theo SanPham
        Map<Integer, List<ChiTietSanPham>> grouped = new LinkedHashMap<>();
        for (ChiTietSanPham v : variants) {
            if (v.getSanPham() == null) continue;
            grouped.computeIfAbsent(v.getSanPham().getId(), k -> new ArrayList<>()).add(v);
        }

        List<Map<String, Object>> productList = new ArrayList<>();
        for (Map.Entry<Integer, List<ChiTietSanPham>> entry : grouped.entrySet()) {
            List<ChiTietSanPham> vList = entry.getValue();
            ChiTietSanPham first = vList.get(0);
            SanPham sp = first.getSanPham();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", sp.getId());
            item.put("maSanPham", sp.getMaSanPham());
            item.put("tenSanPham", sp.getTenSanPham());
            item.put("thuongHieu", sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : null);

            // Giá min/max
            BigDecimal priceMin = vList.stream().map(ChiTietSanPham::getGiaBan).filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal priceMax = vList.stream().map(ChiTietSanPham::getGiaBan).filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            item.put("giaMin", priceMin);
            item.put("giaMax", priceMax);

            // Giá sau giảm
            BigDecimal bestSalePrice = vList.stream()
                    .map(ChiTietSanPham::getGiaSauGiam)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
            item.put("giaSauGiam", bestSalePrice);

            // Phần trăm giảm
            BigDecimal bestDiscount = vList.stream()
                    .map(ChiTietSanPham::getPhanTramGiam)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(null);
            item.put("giamGia", bestDiscount);

            // Tồn kho
            int totalStock = vList.stream().mapToInt(v -> v.getSoLuong() != null ? v.getSoLuong() : 0).sum();
            item.put("tonKho", totalStock);

            // Màu sắc
            List<String> colors = vList.stream()
                    .map(v -> v.getMauSac() != null ? v.getMauSac().getTenMauSac() : null)
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            item.put("mauSacs", colors);

            // Kích thước
            List<String> sizes = vList.stream()
                    .map(v -> v.getKichThuoc() != null ? v.getKichThuoc().getTenKichThuoc() : null)
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            item.put("kichThuocs", sizes);

            // Hình ảnh
            for (ChiTietSanPham v : vList) {
                if (v.getHinhAnhs() != null && !v.getHinhAnhs().isEmpty()) {
                    // Ưu tiên ảnh chính
                    HinhAnh mainImg = v.getHinhAnhs().stream()
                            .filter(h -> Boolean.TRUE.equals(h.getAnhChinh()))
                            .findFirst()
                            .orElse(v.getHinhAnhs().get(0));
                    item.put("hinhAnh", mainImg.getDuongDanAnh());
                    break;
                }
            }

            productList.add(item);
        }
        return productList;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0đ";
        return String.format("%,.0f", amount) + "đ";
    }
}
