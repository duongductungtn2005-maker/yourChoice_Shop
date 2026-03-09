<template>
  <div class="pos-page">
    <!-- ===== TOP BAR ===== -->
    <div class="pos-topbar">
      <div class="topbar-left">
        <div class="pos-title">
          <div class="pos-badge">POS</div>
          <div>
            <h2>Bán hàng tại quầy</h2>
            <p class="pos-sub">
              Quản lý đơn nhanh • Tạo tối đa <b>{{ MAX_TABS }}</b> đơn cùng lúc
            </p>
          </div>
        </div>
      </div>

      <div class="topbar-right">
        <div class="tab-counter">
          <span class="dot" :class="{ on: orderTabs.length > 0 }"></span>
          <span>
            Đang mở: <b>{{ orderTabs.length }}</b>/<b>{{ MAX_TABS }}</b>
          </span>
        </div>

        <button class="btn-primary" @click="createNewTab">
          <span class="btn-icon">＋</span>
          <span>Tạo đơn hàng</span>
        </button>
      </div>
    </div>

    <!-- ===== TAB BAR ===== -->
    <div v-if="orderTabs.length" class="order-tabs">
      <div v-for="tab in orderTabs" :key="tab.id" class="order-tab"
        :class="{ active: tab.id === activeTabId, disabled: showModal }" @click="!showModal && (activeTabId = tab.id)">
        <div class="tab-pill">
          <span class="tab-code">{{ getTabLabel(tab) }}</span>
          <span class="tab-meta" v-if="getTabItemCount(tab) > 0">• {{ getTabItemCount(tab) }} SP</span>
        </div>

        <button class="close-tab" title="Đóng tab" @click.stop="closeTab(tab.id)">×</button>
      </div>
    </div>

    <!-- ===== EMPTY STATE (KHI CHƯA CÓ ĐƠN) ===== -->
    <div v-if="orderTabs.length === 0" class="empty-shell">
      <div class="empty-card">
        <div class="empty-hero">
          <div class="hero-left">
            <h3>Chưa có đơn hàng nào</h3>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== POS MAIN (CHỈ HIỆN KHI CÓ TAB) ===== -->
    <div v-if="currentOrder" class="pos-main-container">
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <h3>Sản phẩm trong hóa đơn</h3>
          </div>
          <button class="btn-primary" @click="openProductModal">+ Thêm sản phẩm</button>
        </div>

        <div class="card-body">
          <div v-if="cart.length === 0" class="empty-cart">
            <div class="empty-icon">🛒</div>
            <div class="empty-text">
              <b>Giỏ hàng đang trống</b>
              <div class="muted">Nhấn “Thêm sản phẩm” để bắt đầu</div>
            </div>
          </div>

          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>Mã SP</th>
                  <th>Tên</th>
                  <th>Thương hiệu</th>
                  <th>Màu sắc</th>
                  <th>Kích thước</th>
                  <th>Giá</th>
                  <th>SL</th>
                  <th>Thành tiền</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, i) in cart" :key="item.id">
                  <td class="mono">{{ item.productCode || item.code }}</td>
                  <td class="name-cell">
                    <div class="name-main">{{ item.name }}</div>
                    <div class="name-sub muted" v-if="item.material || item.coAo || item.tayAo || item.xuatXu">
                      {{ item.material || '—' }} • {{ item.coAo || '—' }} • {{ item.tayAo || '—' }} • {{ item.xuatXu ||
                        '—' }}
                    </div>
                  </td>
                  <td>{{ item.brand || '-' }}</td>
                  <td>{{ item.color || '-' }}</td>
                  <td>{{ item.size || '-' }}</td>
                  <td class="p-price">
                    <div class="price-main">{{ formatMoney(item.price) }}</div>
                    <div v-if="item.priceChangeMeta" class="price-old">{{ formatMoney(item.priceChangeMeta.oldPrice) }}
                    </div>
                  </td>
                  <td>
                    <div>
                      <div class="item-control">
                        <button @click="decreaseCartQty(item)" :disabled="item.qty <= 1" title="Giảm">−</button>
                        <input class="qty-input" :class="{ 'qty-input-error': !!item.qtyWarning }" type="number" min="1"
                          :max="item.qty + item.tonKho" :value="item.qty"
                          @change="updateCartQty(item, $event.target.value)" />
                        <button @click="increaseCartQty(item)" :disabled="item.tonKho <= 0" title="Tăng">＋</button>
                      </div>
                      <div v-if="item.qtyWarning" class="qty-warning">{{ item.qtyWarning }}</div>
                    </div>
                  </td>
                  <td class="p-price">
                    <div class="price-main">{{ formatMoney(item.price * item.qty) }}</div>
                    <div v-if="item.priceChangeMeta" class="price-change-note">
                      <div class="price-change-title">Giá gốc đã thay đổi</div>
                      <div class="price-change-flow">{{ formatMoney(item.priceChangeMeta.oldPrice) }} -> {{
                        formatMoney(item.priceChangeMeta.newPrice) }}</div>
                    </div>
                  </td>
                  <td>
                    <button class="btn-remove" title="Xoá" @click="removeCartItem(i, item)">×</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <!-- ===== LEFT: CART ===== -->
      <div class="pos-cart">

        <!-- ===== CUSTOMER ===== -->
        <div class="card">
          <div class="order-type-toggle">
            <span :class="{ active: orderType === 'TAI_QUAY' }"></span>

            <label class="switch">
              <input type="checkbox" v-model="isDelivery" />
              <span class="slider"></span>
            </label>

            <span :class="{ active: orderType === 'GIAO_HANG' }">Giao hàng</span>
          </div>
          <div class="card-header">
            <div class="card-title">
              <h3>Khách hàng</h3>
              <span class="muted">Thông tin người mua</span>
            </div>
            <div class="customer-actions">
              <button class="btn-outline" @click="openCustomerModal">Chọn khách hàng</button>
              <button class="btn-outline" @click="setGuestCustomer">Khách lẻ</button>
            </div>
          </div>

          <div class="card-body">
            <template v-if="orderType === 'TAI_QUAY'">
              <div class="customer-inline-info">
                <span class="customer-inline-label">Tên khách hàng</span>
                <span class="customer-inline-value">{{ customer.name || 'Khách lẻ' }}</span>
              </div>
              <div class="counter-note muted">Tại quầy: chỉ cần chọn sản phẩm và thanh toán.</div>
            </template>

            <template v-else>
              <div class="customer-inline-info delivery-summary">
                <span class="customer-inline-label">Tên khách hàng:</span>
                <span class="customer-inline-value">{{ customer.name || 'Khách lẻ' }}</span>
              </div>

              <div class="delivery-form-grid">
                <div class="form-field">
                  <label class="field-label">Số điện thoại <span>*</span></label>
                  <input v-model="recipient.phone" placeholder="Nhập số điện thoại..." />
                </div>

                <div class="form-field">
                  <label class="field-label">Địa chỉ cụ thể <span>*</span></label>
                  <input v-model="customer.address" placeholder="Số nhà, ngõ, đường..." />
                </div>
              </div>

              <div class="address-group delivery-address-group">
                <div class="address-field">
                  <label class="address-label">Tỉnh/Thành phố <span>*</span></label>
                  <select v-model="selectedProvince" @change="onProvinceChange" class="address-select">
                    <option value="">Chọn Tỉnh/Thành phố...</option>
                    <option v-for="item in provinces" :key="item.provinceId" :value="item.provinceId">
                      {{ item.provinceName }}
                    </option>
                  </select>
                </div>

                <div class="address-field">
                  <label class="address-label">Quận/Huyện <span>*</span></label>
                  <select v-model="selectedDistrict" @change="onDistrictChange" class="address-select"
                    :disabled="!selectedProvince">
                    <option value="">Chọn Quận/Huyện...</option>
                    <option v-for="item in districts" :key="item.districtId" :value="item.districtId">
                      {{ item.districtName }}
                    </option>
                  </select>
                </div>

                <div class="address-field">
                  <label class="address-label">Xã/Phường <span>*</span></label>
                  <select v-model="selectedWard" @change="onWardChange" class="address-select"
                    :disabled="!selectedDistrict">
                    <option value="">Chọn Xã/Phường...</option>
                    <option v-for="item in wards" :key="item.wardCode" :value="item.wardCode">
                      {{ item.wardName }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="form-field full-width note-field">
                <label class="field-label">Ghi chú</label>
                <textarea v-model="note" placeholder="Nhập ghi chú giao hàng..."></textarea>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- ===== RIGHT: INFO BAR ===== -->
      <div class="pos-info">
        <div class="card payment-card merged-payment-card">
          <div class="card-header">
            <div class="card-title">
              <h3>Thông tin thanh toán</h3>
            </div>
          </div>

          <div class="card-body pay-body">
            <div class="voucher-inline-grid">
              <div class="voucher-inline-field">
                <label>Mã phiếu giảm giá</label>
                <div class="voucher-apply-row">
                  <input v-model="voucherCodeInput" class="voucher-code-input" placeholder="Nhập mã (Enter để áp dụng)"
                    @keyup.enter="applyVoucherByCode" />
                  <button class="btn-apply-code" @click="applyVoucherByCode">Áp dụng</button>
                </div>
              </div>

              <div class="voucher-inline-field">
                <label>Giá trị</label>
                <input class="voucher-value-input" :value="appliedVoucherValue" readonly />
              </div>
            </div>

            <div v-if="voucherAppliedNotice" class="voucher-applied-notice">
              {{ voucherAppliedNotice }}
            </div>

            <div v-if="voucherSuggestion && cart.length > 0" class="voucher-suggestion voucher-suggestion-inline">
              <div class="suggestion-icon">💡</div>
              <div class="suggestion-text">
                Mua thêm <b>{{ formatMoney(voucherSuggestion.additionalNeeded) }}</b> để áp dụng phiếu
                <b>{{ voucherSuggestion.voucher.name }}</b>
                <span class="suggestion-detail">
                  (Giảm {{ voucherSuggestion.voucher.type === 'percent'
                    ? voucherSuggestion.voucher.value + '%'
                    : formatMoney(voucherSuggestion.voucher.value) }})
                </span>
              </div>
            </div>

            <div class="row">
              <span>Tiền hàng</span>
              <span class="price-col">{{ formatMoney(totalProductPrice) }}</span>
            </div>

            <div class="row">
              <span>Giảm giá</span>
              <span class="price-col">- {{ formatMoney(totalDiscount) }}</span>
            </div>

            <div class="row" v-if="orderType === 'GIAO_HANG'">
              <span class="shipping-label">
                Phí vận chuyển
                <img class="shipping-logo" :src="ghnLogo" alt="GHN Express" />
              </span>
              <div class="shipping-control-wrap">
                <input type="number" min="0" v-model.number="shippingFee" class="price-col ship-input"
                  placeholder="0" />
                <button class="ship-refresh-btn" @click="refreshShippingFee" title="Tính lại phí">↻</button>
                <span class="ship-unit">đ</span>
              </div>
            </div>

            <div v-if="orderType === 'GIAO_HANG' && !isDeliveryAddressReady" class="shipping-hint">
              Chưa đủ địa chỉ để tính phí GHN.
            </div>

            <div class="row total-row">
              <span>Tổng số tiền</span>
              <span class="price-col total">{{ formatMoney(totalPrice) }}</span>
            </div>

            <button v-if="orderType === 'TAI_QUAY'" class="btn-pay" @click="openPaymentModal">
              THANH TOÁN
            </button>

            <button v-else class="btn-pay" @click="handleCreateOrderDelivery">
              TẠO HÓA ĐƠN
            </button>

            <div class="pay-note muted">
              Lưu ý: Tại quầy có thể chọn khách lẻ, giao hàng cần thông tin người nhận đầy đủ.
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== MODAL SẢN PHẨM ===== -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content large">
        <div class="modal-header-flex">
          <h3>Chọn sản phẩm</h3>
          <button class="close-btn" @click="showModal = false">×</button>
        </div>

        <div class="product-filter-grid">
          <div class="filter-item">
            <label>Tìm mã / tên:</label>
            <input v-model="productKeyword" class="search-input"
              placeholder="Nhập mã SP cha, mã SKU hoặc tên sản phẩm" />
          </div>
          <div class="filter-item">
            <label>Cổ áo:</label>
            <select v-model="productFilter.coAo" class="address-select">
              <option value="">Tất cả</option>
              <option v-for="o in productFilterOptions.coAo" :key="o" :value="o">{{ o }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Tay áo:</label>
            <select v-model="productFilter.tayAo" class="address-select">
              <option value="">Tất cả</option>
              <option v-for="o in productFilterOptions.tayAo" :key="o" :value="o">{{ o }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Xuất xứ:</label>
            <select v-model="productFilter.xuatXu" class="address-select">
              <option value="">Tất cả</option>
              <option v-for="o in productFilterOptions.xuatXu" :key="o" :value="o">{{ o }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Kích thước:</label>
            <select v-model="productFilter.size" class="address-select">
              <option value="">Tất cả</option>
              <option v-for="o in productFilterOptions.size" :key="o" :value="o">{{ o }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Thương hiệu:</label>
            <select v-model="productFilter.brand" class="address-select">
              <option value="">Tất cả</option>
              <option v-for="o in productFilterOptions.brand" :key="o" :value="o">{{ o }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Màu sắc:</label>
            <select v-model="productFilter.color" class="address-select">
              <option value="">Tất cả</option>
              <option v-for="o in productFilterOptions.color" :key="o" :value="o">{{ o }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Chất liệu:</label>
            <select v-model="productFilter.material" class="address-select">
              <option value="">Tất cả</option>
              <option v-for="o in productFilterOptions.material" :key="o" :value="o">{{ o }}</option>
            </select>
          </div>
        </div>

        <div class="price-range slider-wrap">
          <span>{{ formatMoney(0) }}</span>
          <input type="range" min="0" :max="maxPriceFilter" step="1000" v-model.number="priceRange[1]"
            class="price-slider" />
          <span>{{ formatMoney(priceRange[1]) }}</span>
        </div>

        <div class="modal-table-wrapper">
          <table class="table modal-table">
            <thead>
              <tr>
                <th class="product-code-head">Mã sản phẩm</th>
                <th>Tên</th>
                <th>Thương hiệu</th>
                <th>Chất liệu</th>
                <th>Tồn kho</th>
                <th>Giá</th>
                <th class="product-qty-head">SL</th>
                <th class="product-check-head"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filteredProducts.length === 0">
                <td colspan="8" class="empty-state">Không có sản phẩm đang kinh doanh phù hợp bộ lọc.</td>
              </tr>
              <tr v-for="p in filteredProducts" :key="p.id">
                <td class="product-code-cell"><span class="mono">{{ p.productCode || p.code }}</span></td>
                <td class="name-cell">
                  <div class="name-main">{{ p.name }}</div>
                  <div class="muted">{{ p.color || '-' }} • {{ p.size || '-' }}</div>
                </td>
                <td>{{ p.brand || '-' }}</td>
                <td>{{ p.material || '-' }}</td>
                <td>{{ p.tonKho }}</td>
                <td class="p-price">{{ formatMoney(p.price) }}</td>
                <td class="product-qty-cell">
                  <div>
                    <div class="item-control">
                      <button @click="decreaseProductQty(p)" :disabled="p.qty <= 1">−</button>
                      <input class="qty-input" :class="{ 'qty-input-error': !!p.qtyWarning }" type="number" min="1"
                        :max="p.tonKho" :value="p.qty" @input="updateProductQty(p, $event.target.value)" />
                      <button @click="increaseProductQty(p)" :disabled="p.qty >= p.tonKho">＋</button>
                    </div>
                    <div v-if="p.qtyWarning" class="qty-warning">{{ p.qtyWarning }}</div>
                  </div>
                </td>
                <td class="product-check-cell"><input class="product-check" type="checkbox" v-model="p.checked" /></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button @click="productPage--" :disabled="productPage === 0">‹ Trước</button>
          <span>Trang {{ productPage + 1 }} / {{ totalProductPages }}</span>
          <button @click="productPage++" :disabled="productPage + 1 >= totalProductPages">Sau ›</button>
        </div>

        <div class="modal-actions">
          <button class="btn-outline" @click="showModal = false">Hủy</button>
          <button class="btn-primary" @click="confirmAddProduct">Thêm</button>
        </div>
      </div>
    </div>

    <!-- ===== MODAL KHÁCH HÀNG ===== -->
    <div v-if="showCustomerModal" class="modal-overlay">
      <div class="modal-content customer-modal">
        <div class="modal-header-flex">
          <h3>Chọn khách hàng</h3>
          <button class="close-btn" @click="showCustomerModal = false">×</button>
        </div>

        <input v-model="customerKeyword" class="search-input" placeholder="Tìm tên / SĐT / email" />

        <div class="modal-table-wrapper">
          <table class="table modal-table customer-table">
            <colgroup>
              <col class="col-name" />
              <col class="col-phone" />
              <col class="col-email" />
              <col class="col-action" />
            </colgroup>
            <thead>
              <tr>
                <th>Tên KH</th>
                <th>SĐT</th>
                <th>Email</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in filteredCustomers" :key="c.id">
                <td class="name-cell">
                  <div class="name-main">{{ c.name }}</div>
                </td>
                <td class="phone-cell">{{ c.phone }}</td>
                <td class="email-cell">{{ c.email }}</td>
                <td class="action-cell">
                  <button class="btn-select" @click="selectCustomer(c)">Chọn</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button @click="customerPage--" :disabled="customerPage === 0">‹ Trước</button>
          <span>Trang {{ customerPage + 1 }} / {{ totalCustomerPages }}</span>
          <button @click="customerPage++" :disabled="customerPage + 1 >= totalCustomerPages">Sau ›</button>
        </div>
      </div>
    </div>


    <!-- ===== MODAL THANH TOÁN ===== -->
    <div v-if="showPaymentModal" class="modal-overlay">
      <div class="payment-modal">
        <div class="modal-header-flex">
          <h3>Thanh toán</h3>
          <button class="close-btn" @click="showPaymentModal = false">×</button>
        </div>

        <div class="payment-tabs">
          <div class="tab-item" :class="{ active: paymentMethod === 'TRANSFER' }" @click="paymentMethod = 'TRANSFER'">
            Chuyển khoản
          </div>

          <div class="tab-item" :class="{ active: paymentMethod === 'CASH' }" @click="paymentMethod = 'CASH'">
            Tiền mặt
          </div>
        </div>

        <div v-if="paymentMethod === 'CASH'">
          <input v-model.number="customerCash" type="number" class="search-input" placeholder="Tiền khách đưa" />
          <div class="p-price">Còn lại: {{ formatMoney(calculateRemaining) }}</div>
        </div>

        <div v-if="paymentMethod === 'TRANSFER'" class="qr-section">
          <div class="qr-code">
            <img :src="qrImageUrl" alt="QR chuyển khoản" />
          </div>

          <div class="p-price">Số tiền: {{ formatMoney(totalPrice) }}</div>

          <div style="font-size: 13px; color: #6b7280">
            Nội dung: THANH TOAN HOA DON
          </div>
        </div>

        <div class="payment-footer">
          <button class="btn-pay" @click="confirmCreateOrder">
            {{ orderType === 'TAI_QUAY' ? 'THANH TOÁN' : 'TẠO ĐƠN GIAO HÀNG' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ===== MODAL NHÂN VIÊN ===== -->
    <div v-if="showStaffModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header-flex">
          <h3>Chọn nhân viên</h3>
          <button class="close-btn" @click="showStaffModal = false">×</button>
        </div>

        <input v-model="staffKeyword" class="search-input" placeholder="Tìm mã / tên nhân viên" />

        <div class="modal-table-wrapper">
          <table class="table modal-table">
            <thead>
              <tr>
                <th>Mã NV</th>
                <th>Tên NV</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in filteredStaffs" :key="s.id">
                <td class="mono">{{ s.code }}</td>
                <td class="name-cell">
                  <div class="name-main">{{ s.name }}</div>
                </td>
                <td>
                  <button class="btn-select" @click="selectStaff(s)">Chọn</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button @click="staffPage--" :disabled="staffPage === 0">‹ Trước</button>
          <span>Trang {{ staffPage + 1 }} / {{ totalStaffPages }}</span>
          <button @click="staffPage++" :disabled="staffPage + 1 >= totalStaffPages">Sau ›</button>
        </div>
      </div>
    </div>

    <!-- ===== MODAL THÔNG BÁO PHIẾU GIẢM GIÁ MỚI TỐT HƠN ===== -->
    <div v-if="showNewVoucherModal" class="modal-overlay">
      <div class="modal-content voucher-notify-modal">
        <div class="modal-header-flex">
          <h3>🎉 Phiếu giảm giá mới!</h3>
          <button class="close-btn" @click="keepOldVoucher">×</button>
        </div>

        <div class="voucher-notify-body">
          <p>Có phiếu giảm giá mới <b>{{ newBetterVoucher?.name }}</b> với giá trị tốt hơn phiếu đang áp dụng!</p>

          <div class="voucher-compare">
            <div class="voucher-old">
              <div class="compare-label">Phiếu hiện tại</div>
              <div class="compare-value">-{{ formatMoney(newBetterVoucher?.currentDiscount || 0) }}</div>
            </div>
            <div class="compare-arrow">→</div>
            <div class="voucher-new">
              <div class="compare-label">Phiếu mới</div>
              <div class="compare-value highlight">-{{ formatMoney(newBetterVoucher?.calculatedDiscount || 0) }}</div>
              <div class="compare-name">{{ newBetterVoucher?.name }}</div>
            </div>
          </div>

          <p class="compare-benefit">
            Tiết kiệm thêm <b>{{ formatMoney((newBetterVoucher?.calculatedDiscount || 0) -
              (newBetterVoucher?.currentDiscount || 0)) }}</b>
          </p>
        </div>

        <div class="modal-actions">
          <button class="btn-outline" @click="keepOldVoucher">Giữ phiếu cũ</button>
          <button class="btn-primary" @click="applyNewVoucher">Áp dụng phiếu mới</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { createOrder, createPosDraftOrder, deletePosDraftOrder } from '@/api/HoaDonApi'
import { getCurrentUser, getRole } from '@/services/auth'
import ghnLogo from '@/img/Logo_GHN.webp'

/* ================= FILTER SẢN PHẨM ================= */
const productKeyword = ref('')
const priceRange = ref([0, 1000000])
const productFilter = ref({
  coAo: '',
  tayAo: '',
  xuatXu: '',
  size: '',
  brand: '',
  color: '',
  material: ''
})

/* ================= ROUTER ================= */
const router = useRouter()

// Computed để chọn route name dựa trên role
const orderListRouteName = computed(() => {
  const role = getRole()
  return role === 'STAFF' ? 'staff-order-list' : 'admin-order-list'
})

/* ================= MODAL KHÁCH HÀNG ================= */
const showCustomerModal = ref(false)
const customerKeyword = ref('')

import { getKhachHang } from '@/api/KhachHangApi'

const customers = ref([])
const filteredCustomers = computed(() =>
  customers.value.filter(c =>
    [c.name, c.phone, c.email]
      .join(' ')
      .toLowerCase()
      .includes(customerKeyword.value.toLowerCase())
  )
)

const loadCustomers = async () => {
  const res = await getKhachHang({
    page: customerPage.value,
    size: customerSize.value,
    trangThai: 1
  })

  customers.value = res.data.content.map(c => ({
    id: c.id,
    code: c.maKhachHang,
    name: c.tenKhachHang,
    phone: c.soDienThoai,
    email: c.email
  }))

  totalCustomerPages.value = res.data.totalPages
}

const customerPage = ref(0)
const customerSize = ref(10)
const totalCustomerPages = ref(0)

const openCustomerModal = async () => {
  await loadCustomers()
  showCustomerModal.value = true
}

const selectCustomer = (c) => {
  customer.value = {
    name: c.name,
    phone: c.phone,
    email: c.email,
    address: ''
  }
  showCustomerModal.value = false
}

const setGuestCustomer = () => {
  customer.value = {
    name: 'Khách lẻ',
    phone: '',
    email: '',
    address: ''
  }
}

/* ================= MODAL SẢN PHẨM ================= */
const showModal = ref(false)

import { getChiTietSanPham, reserveStock, releaseStock } from '@/api/ChiTietSanPhamApi'

const products = ref([])
const productFilterSource = ref([])
const productAttributeOptions = ref({
  coAo: [],
  tayAo: [],
  xuatXu: [],
  size: [],
  brand: [],
  color: [],
  material: []
})
const productPage = ref(0)
const productSize = ref(10)
const totalProductPages = ref(0)

const ATTRIBUTE_API_MAP = {
  coAo: { url: 'http://localhost:8080/api/v1/co-ao', nameKey: 'tenCoAo' },
  tayAo: { url: 'http://localhost:8080/api/v1/tay-ao', nameKey: 'tenTayAo' },
  xuatXu: { url: 'http://localhost:8080/api/v1/xuat-xu', nameKey: 'tenXuatXu' },
  size: { url: 'http://localhost:8080/api/v1/kich-thuoc', nameKey: 'tenKichThuoc' },
  brand: { url: 'http://localhost:8080/api/v1/thuong-hieu', nameKey: 'tenThuongHieu' },
  color: { url: 'http://localhost:8080/api/v1/mau-sac', nameKey: 'tenMauSac' },
  material: { url: 'http://localhost:8080/api/v1/chat-lieu', nameKey: 'tenChatLieu' }
}

const fetchAllAttributeNames = async (apiUrl, nameKey) => {
  let page = 0
  const size = 200
  let totalPages = 1
  const collectedNames = []

  while (page < totalPages) {
    const res = await axios.get(apiUrl, {
      params: { page, size, keyword: '' }
    })

    const items = Array.isArray(res?.data?.content) ? res.data.content : []
    const activeItems = items.filter(item => Number(item?.trangThai) === 1)
    collectedNames.push(
      ...activeItems
        .map(item => item?.[nameKey])
        .filter(Boolean)
    )

    totalPages = Number(res?.data?.totalPages || 1)
    page += 1
  }

  return [...new Set(collectedNames)]
}

const loadProductAttributeOptions = async () => {
  const entries = await Promise.all(
    Object.entries(ATTRIBUTE_API_MAP).map(async ([key, config]) => {
      try {
        const names = await fetchAllAttributeNames(config.url, config.nameKey)
        return [key, names]
      } catch (error) {
        console.error(`Không thể tải danh mục bộ lọc ${key}:`, error)
        return [key, []]
      }
    })
  )

  productAttributeOptions.value = Object.fromEntries(entries)
}

const isActiveProductDetail = (detail) => {
  const detailStatus = Number(detail?.trangThai)
  const parentStatus = Number(detail?.sanPham?.trangThai)
  return detailStatus === 1 && parentStatus === 1
}

const mapProductDetail = (p) => {
  const inCart = cart.value.find(i => i.id === p.id)
  const parentProduct = p.sanPham || {}

  return {
    id: p.id,
    code: p.maCtsp,
    productCode: parentProduct.maSanPham || '',
    name: parentProduct.tenSanPham || '',
    brand: p.thuongHieu?.tenThuongHieu || parentProduct.thuongHieu?.tenThuongHieu || '—',
    material: p.chatLieu?.tenChatLieu || parentProduct.chatLieu?.tenChatLieu || '—',
    color: p.mauSac?.tenMauSac || '—',
    size: p.kichThuoc?.tenKichThuoc || '—',
    coAo: p.coAo?.tenCoAo || parentProduct.coAo?.tenCoAo || '—',
    tayAo: p.tayAo?.tenTayAo || parentProduct.tayAo?.tenTayAo || '—',
    xuatXu: p.xuatXu?.tenXuatXu || parentProduct.xuatXu?.tenXuatXu || '—',
    price: p.giaBan,
    tonKho: p.soLuong,
    qty: inCart ? Number(inCart.qty || 1) : 1,
    checked: false,
    qtyWarning: ''
  }
}

const loadProducts = async () => {
  const res = await getChiTietSanPham({
    page: productPage.value,
    size: productSize.value,
    trangThai: 1
  })

  products.value = (res?.data?.content || [])
    .filter(isActiveProductDetail)
    .map(mapProductDetail)

  totalProductPages.value = res.data.totalPages
}

const loadProductFilterSource = async () => {
  let page = 0
  const size = 200
  let totalPages = 1
  const allItems = []

  while (page < totalPages) {
    const res = await getChiTietSanPham({
      page,
      size,
      trangThai: 1
    })

    const pageItems = (res?.data?.content || []).filter(isActiveProductDetail)
    allItems.push(...pageItems)

    totalPages = Number(res?.data?.totalPages || 1)
    page += 1
  }

  productFilterSource.value = allItems.map(mapProductDetail)
}

const openProductModal = async () => {
  productPage.value = 0
  productKeyword.value = ''
  productFilter.value = {
    coAo: '',
    tayAo: '',
    xuatXu: '',
    size: '',
    brand: '',
    color: '',
    material: ''
  }

  await Promise.all([loadProducts(), loadProductFilterSource(), loadProductAttributeOptions()])

  const maxPrice = Math.max(...productFilterSource.value.map(p => Number(p.price || 0)), 0)
  priceRange.value = [0, maxPrice]
  showModal.value = true
}

const getErrorMessage = (error, fallbackMessage) => {
  return error?.response?.data || fallbackMessage
}

const reserveProductStock = async (id, soLuong, showError = true) => {
  if (!Number.isFinite(soLuong) || soLuong <= 0) {
    return false
  }

  try {
    await reserveStock(id, soLuong)
    return true
  } catch (error) {
    if (showError) {
      alert(getErrorMessage(error, 'Không thể giữ kho. Vui lòng thử lại.'))
    }
    return false
  }
}

const releaseProductStock = async (id, soLuong, showError = true) => {
  if (!Number.isFinite(soLuong) || soLuong <= 0) {
    return false
  }

  try {
    await releaseStock(id, soLuong)
    return true
  } catch (error) {
    if (showError) {
      alert(getErrorMessage(error, 'Không thể hoàn kho. Vui lòng thử lại.'))
    }
    return false
  }
}

const normalizeQty = (value, maxQty) => {
  const parsed = Number.parseInt(value, 10)
  if (!Number.isFinite(parsed) || parsed < 1) {
    return 1
  }

  const max = Number.isFinite(maxQty) && maxQty > 0 ? maxQty : 1
  return Math.min(parsed, max)
}

const getQtyWarning = (value, maxQty) => {
  const parsed = Number.parseInt(value, 10)
  if (!Number.isFinite(parsed) || parsed < 1) {
    return 'Số lượng phải lớn hơn 0'
  }
  if (parsed > maxQty) {
    return `Vượt tồn kho (tối đa ${maxQty})`
  }
  return ''
}

const updateCartQty = async (item, value) => {
  const currentQty = Number(item.qty) || 1
  const maxQty = currentQty + Number(item.tonKho || 0)
  const warning = getQtyWarning(value, maxQty)
  const nextQty = normalizeQty(value, maxQty)

  item.qtyWarning = warning

  if (nextQty === currentQty) {
    item.qty = nextQty
    return
  }

  if (nextQty > currentQty) {
    const delta = nextQty - currentQty
    const reserved = await reserveProductStock(item.id, delta)
    if (!reserved) {
      item.qtyWarning = 'Không đủ tồn kho'
      item.qty = currentQty
      return
    }
    item.tonKho = Math.max(0, Number(item.tonKho || 0) - delta)
  } else {
    const delta = currentQty - nextQty
    const released = await releaseProductStock(item.id, delta)
    if (!released) {
      item.qty = currentQty
      return
    }
    item.tonKho = Number(item.tonKho || 0) + delta
  }

  if (!warning) {
    item.qtyWarning = ''
  }
  item.qty = nextQty
}

const increaseCartQty = async (item) => {
  if (Number(item.tonKho) <= 0) {
    item.qtyWarning = 'Không đủ tồn kho'
    return
  }

  const reserved = await reserveProductStock(item.id, 1)
  if (!reserved) {
    item.qtyWarning = 'Không đủ tồn kho'
    return
  }

  item.qtyWarning = ''
  item.qty = Number(item.qty || 0) + 1
  item.tonKho = Math.max(0, Number(item.tonKho || 0) - 1)
}

const decreaseCartQty = async (item) => {
  if (Number(item.qty) <= 1) return

  const released = await releaseProductStock(item.id, 1)
  if (!released) {
    return
  }

  item.qtyWarning = ''
  item.qty = Number(item.qty || 1) - 1
  item.tonKho = Number(item.tonKho || 0) + 1
}

const updateProductQty = (product, value) => {
  product.qtyWarning = getQtyWarning(value, product.tonKho)
  product.qty = normalizeQty(value, product.tonKho)
}

const increaseProductQty = (product) => {
  product.qtyWarning = product.qty >= product.tonKho ? `Vượt tồn kho (tối đa ${product.tonKho})` : ''
  product.qty = normalizeQty(product.qty + 1, product.tonKho)
}

const decreaseProductQty = (product) => {
  product.qtyWarning = ''
  product.qty = normalizeQty(product.qty - 1, product.tonKho)
}

const hasInvalidCartQty = () => {
  return cart.value.some(item => {
    const qty = Number(item.qty)
    return !Number.isFinite(qty) || qty < 1
  })
}

const confirmAddProduct = async () => {
  for (const p of products.value) {
    if (!p.checked) continue

    const normalizedQty = normalizeQty(p.qty, p.tonKho)
    if (p.tonKho <= 0) {
      p.checked = false
      p.qty = 1
      return
    }

    const reserved = await reserveProductStock(p.id, normalizedQty)
    if (!reserved) {
      p.checked = false
      p.qty = 1
      continue
    }

    const exist = cart.value.find(i => i.id === p.id)

    if (exist) {
      // Sản phẩm đã có trong hoá đơn: giữ nguyên giá cũ, chỉ cập nhật số lượng
      exist.qty = Number(exist.qty || 0) + normalizedQty
      exist.tonKho = Math.max(0, Number(exist.tonKho || 0) - normalizedQty)
      exist.qtyWarning = ''
    } else {
      // Sản phẩm mới: áp dụng giá hiện tại từ hệ thống
      cart.value.push({
        id: p.id,
        code: p.code,
        productCode: p.productCode,
        name: p.name,
        brand: p.brand,
        material: p.material,
        color: p.color,
        size: p.size,
        coAo: p.coAo,
        tayAo: p.tayAo,
        xuatXu: p.xuatXu,
        price: p.price,
        qty: normalizedQty,
        tonKho: Math.max(0, Number(p.tonKho || 0) - normalizedQty),
        qtyWarning: ''
      })
    }

    p.tonKho = Math.max(0, Number(p.tonKho || 0) - normalizedQty)

    p.checked = false
    p.qty = 1
  }

  await loadProducts()
  showModal.value = false
}

const removeCartItem = async (index, item) => {
  const qtyToRelease = Number(item.qty || 0)
  if (qtyToRelease <= 0) {
    cart.value.splice(index, 1)
    return
  }

  const released = await releaseProductStock(item.id, qtyToRelease)
  if (!released) {
    return
  }

  cart.value.splice(index, 1)
}

const filteredProducts = computed(() =>
  products.value.filter(p => {
    const keyword = productKeyword.value.toLowerCase()

    const matchKeyword =
      (p.name || '').toLowerCase().includes(keyword) ||
      (p.code && p.code.toLowerCase().includes(keyword)) ||
      (p.productCode && p.productCode.toLowerCase().includes(keyword))

    const f = productFilter.value
    const matchCoAo = !f.coAo || p.coAo === f.coAo
    const matchTayAo = !f.tayAo || p.tayAo === f.tayAo
    const matchXuatXu = !f.xuatXu || p.xuatXu === f.xuatXu
    const matchSize = !f.size || p.size === f.size
    const matchBrand = !f.brand || p.brand === f.brand
    const matchColor = !f.color || p.color === f.color
    const matchMaterial = !f.material || p.material === f.material

    const matchPrice =
      p.price >= priceRange.value[0] &&
      p.price <= priceRange.value[1]

    return (
      matchKeyword &&
      matchPrice &&
      matchCoAo &&
      matchTayAo &&
      matchXuatXu &&
      matchSize &&
      matchBrand &&
      matchColor &&
      matchMaterial
    )
  })
)

const makeFilterOptions = (key) => {
  return [...new Set(productFilterSource.value.map(p => p[key]).filter(v => v && v !== '—'))]
}

const productFilterOptions = computed(() => ({
  coAo: productAttributeOptions.value.coAo.length > 0 ? productAttributeOptions.value.coAo : makeFilterOptions('coAo'),
  tayAo: productAttributeOptions.value.tayAo.length > 0 ? productAttributeOptions.value.tayAo : makeFilterOptions('tayAo'),
  xuatXu: productAttributeOptions.value.xuatXu.length > 0 ? productAttributeOptions.value.xuatXu : makeFilterOptions('xuatXu'),
  size: productAttributeOptions.value.size.length > 0 ? productAttributeOptions.value.size : makeFilterOptions('size'),
  brand: productAttributeOptions.value.brand.length > 0 ? productAttributeOptions.value.brand : makeFilterOptions('brand'),
  color: productAttributeOptions.value.color.length > 0 ? productAttributeOptions.value.color : makeFilterOptions('color'),
  material: productAttributeOptions.value.material.length > 0 ? productAttributeOptions.value.material : makeFilterOptions('material')
}))

const maxPriceFilter = computed(() => {
  return Math.max(...productFilterSource.value.map(p => Number(p.price || 0)), 0)
})

/* ================= GIẢM GIÁ ================= */
const showDiscountModal = ref(false)
const discountKeyword = ref('')

import { getPhieuGiamGia } from '@/api/PhieuGiamGiaApi'

const discountList = ref([])

const discountPage = ref(0)
const discountSize = ref(10)
const totalDiscountPages = ref(0)

const loadDiscounts = async () => {
  const res = await getPhieuGiamGia({
    page: discountPage.value,
    size: discountSize.value,
    trangThai: 1
  })

  discountList.value = res.data.content.map(d => ({
    id: d.id,
    code: d.maPhieuGiamGia,
    name: d.tenPhieuGiamGia,
    type: d.loaiPhieu === 'PhanTram' ? 'percent' : 'money',
    value: d.giaTriGiam,
    maxDiscount: d.giaTriGiamToiDa || 0,
    minOrder: d.donHangToiThieu || 0,
    soLuong: d.soLuong || 0,
    startDate: d.ngayBatDau,
    endDate: d.ngayKetThuc,
    trangThai: d.trangThai,
    checked: false
  }))

  totalDiscountPages.value = res.data.totalPages
}

const parseVoucherDate = (value) => {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

const isVoucherInDateRange = (voucher) => {
  const now = new Date()
  const start = parseVoucherDate(voucher.startDate)
  const end = parseVoucherDate(voucher.endDate)

  if (start && now < start) return false
  if (end && now > end) return false
  return true
}

const isVoucherActiveNow = (voucher) => {
  return Number(voucher.trangThai) === 1 && isVoucherInDateRange(voucher)
}

const filteredDiscounts = computed(() =>
  discountList.value.filter(d =>
    isVoucherActiveNow(d) &&
    [d.code, d.name]
      .join(' ')
      .toLowerCase()
      .includes(discountKeyword.value.toLowerCase())
  )
)

const openDiscountModal = async () => {
  await loadDiscounts()
  showDiscountModal.value = true
}

const confirmAddDiscount = () => {
  discountList.value.forEach(d => {
    if (d.checked && !discounts.value.find(x => x.id === d.id)) {
      discounts.value.push({
        id: d.id,
        code: d.code,
        name: d.name,
        type: d.type,
        value: d.value,
        maxDiscount: d.maxDiscount,
        minOrder: d.minOrder,
        startDate: d.startDate,
        endDate: d.endDate
      })
    }
    d.checked = false
  })

  showDiscountModal.value = false
}

/* ================= TỰ ĐỘNG CHỌN PHIẾU GIẢM GIÁ TỐT NHẤT ================= */
const allActiveVouchers = ref([])
const showNewVoucherModal = ref(false)
const newBetterVoucher = ref(null)
const voucherCodeInput = ref('')
let voucherPollTimer = null
let pricePollTimer = null
let lastVoucherCoreSnapshot = ''
const VOUCHER_POLL_INTERVAL_MS = 3000

const buildVoucherCoreSnapshot = (vouchers) => {
  if (!Array.isArray(vouchers) || vouchers.length === 0) return ''

  return vouchers
    .map(v => ({
      id: Number(v.id || 0),
      soLuong: Number(v.soLuong || 0),
      value: Number(v.value || 0),
      minOrder: Number(v.minOrder || 0)
    }))
    .sort((a, b) => a.id - b.id)
    .map(v => `${v.id}:${v.soLuong}:${v.value}:${v.minOrder}`)
    .join('|')
}

const loadAllActiveVouchers = async () => {
  try {
    const res = await getPhieuGiamGia({
      page: 0,
      size: 1000,
      trangThai: 1
    })
    const mappedVouchers = res.data.content.map(d => ({
      id: d.id,
      code: d.maPhieuGiamGia,
      name: d.tenPhieuGiamGia,
      type: d.loaiPhieu === 'PhanTram' ? 'percent' : 'money',
      value: Number(d.giaTriGiam || 0),
      maxDiscount: Number(d.giaTriGiamToiDa || 0),
      minOrder: Number(d.donHangToiThieu || 0),
      soLuong: Number(d.soLuong || 0),
      startDate: d.ngayBatDau,
      endDate: d.ngayKetThuc,
      trangThai: d.trangThai
    }))

    const nextSnapshot = buildVoucherCoreSnapshot(mappedVouchers)
    const hasCoreChanges = nextSnapshot !== lastVoucherCoreSnapshot

    allActiveVouchers.value = mappedVouchers
    lastVoucherCoreSnapshot = nextSnapshot

    return hasCoreChanges
  } catch (e) {
    console.error('Lỗi tải phiếu giảm giá:', e)
    return false
  }
}

const syncAppliedDiscountWithActiveVouchers = () => {
  if (!Array.isArray(discounts.value) || discounts.value.length === 0) return

  const activeMap = new Map(
    allActiveVouchers.value
      .filter(isVoucherActiveNow)
      .map(v => [v.id, v])
  )
  const removedCodes = []

  const nextDiscounts = discounts.value
    .map(d => {
      const latest = activeMap.get(d.id)
      if (!latest) {
        removedCodes.push(d.code || d.name || `ID ${d.id}`)
        return null
      }

      return {
        id: latest.id,
        code: latest.code,
        name: latest.name,
        type: latest.type,
        value: latest.value,
        maxDiscount: latest.maxDiscount,
        minOrder: latest.minOrder,
        startDate: latest.startDate,
        endDate: latest.endDate
      }
    })
    .filter(Boolean)

  if (removedCodes.length > 0) {
    alert(`Phiếu giảm giá ${removedCodes.join(', ')} đã ngừng hoạt động và đã được gỡ khỏi đơn.`)
  }

  discounts.value = nextDiscounts
}

const calculateVoucherDiscount = (voucher, cartTotal) => {
  if (cartTotal < voucher.minOrder) return 0
  if (voucher.soLuong <= 0) return 0

  if (voucher.type === 'percent') {
    let disc = cartTotal * voucher.value / 100
    if (voucher.maxDiscount > 0) {
      disc = Math.min(disc, voucher.maxDiscount)
    }
    return Math.min(disc, cartTotal)
  } else {
    return Math.min(voucher.value, cartTotal)
  }
}

const getCartTotalByItems = (items) => {
  if (!Array.isArray(items) || items.length === 0) return 0
  return items.reduce((sum, item) => {
    const price = Number(item?.price || 0)
    const qty = Number(item?.qty || 0)
    return sum + (Number.isFinite(price * qty) ? price * qty : 0)
  }, 0)
}

const pickRandomItem = (items) => {
  if (!Array.isArray(items) || items.length === 0) return null
  const index = Math.floor(Math.random() * items.length)
  return items[index]
}

const toAppliedVoucher = (voucher) => ({
  id: voucher.id,
  code: voucher.code,
  name: voucher.name,
  type: voucher.type,
  value: voucher.value,
  maxDiscount: voucher.maxDiscount,
  minOrder: voucher.minOrder,
  startDate: voucher.startDate,
  endDate: voucher.endDate
})

const applyVoucherByCode = async () => {
  const rawCode = (voucherCodeInput.value || '').trim()

  if (allActiveVouchers.value.length === 0) {
    await loadAllActiveVouchers()
  }

  // Không nhập mã: tự động chọn phiếu tốt nhất có thể áp dụng ở thời điểm hiện tại.
  if (!rawCode) {
    const cartTotal = totalProductPrice.value
    const bestVoucher = allActiveVouchers.value
      .filter(isVoucherActiveNow)
      .map(v => ({ voucher: v, discountAmount: calculateVoucherDiscount(v, cartTotal) }))
      .filter(item => item.discountAmount > 0)
      .sort((a, b) => b.discountAmount - a.discountAmount)[0]?.voucher

    if (!bestVoucher) {
      alert('Hiện chưa có phiếu giảm giá nào phù hợp với đơn hàng.')
      return
    }

    discounts.value = [toAppliedVoucher(bestVoucher)]
    voucherCodeInput.value = bestVoucher.code || ''
    return
  }

  const normalizedCode = rawCode.toLowerCase()
  const matchedVoucher = allActiveVouchers.value.find(v =>
    String(v.code || '').toLowerCase() === normalizedCode
  )

  if (!matchedVoucher || !isVoucherActiveNow(matchedVoucher)) {
    alert('Mã phiếu không tồn tại hoặc đã hết hiệu lực.')
    return
  }

  const discountAmount = calculateVoucherDiscount(matchedVoucher, totalProductPrice.value)
  if (discountAmount <= 0) {
    if (totalProductPrice.value < Number(matchedVoucher.minOrder || 0)) {
      alert(`Đơn hàng chưa đủ điều kiện tối thiểu ${formatMoney(matchedVoucher.minOrder || 0)} để áp dụng mã này.`)
      return
    }

    alert('Phiếu giảm giá này hiện không thể áp dụng cho đơn hàng.')
    return
  }

  discounts.value = [toAppliedVoucher(matchedVoucher)]
  voucherCodeInput.value = matchedVoucher.code || rawCode
}

const autoSelectBestVoucher = () => {
  const cartTotal = totalProductPrice.value
  const activeVouchers = allActiveVouchers.value.filter(isVoucherActiveNow)

  if (cartTotal <= 0 || activeVouchers.length === 0) {
    if (discounts.value.length > 0) {
      discounts.value = []
    }
    return
  }

  const eligibleVouchers = activeVouchers
    .map(v => ({
      voucher: v,
      discountAmount: calculateVoucherDiscount(v, cartTotal)
    }))
    .filter(item => item.discountAmount > 0)

  const bestDiscount = Math.max(...eligibleVouchers.map(item => item.discountAmount), 0)
  const bestCandidates = eligibleVouchers
    .filter(item => item.discountAmount === bestDiscount)
    .map(item => item.voucher)

  const bestVoucher = pickRandomItem(bestCandidates)

  if (bestVoucher) {
    const currentId = discounts.value.length === 1 ? discounts.value[0].id : null
    if (currentId !== bestVoucher.id) {
      discounts.value = [toAppliedVoucher(bestVoucher)]
    }
  } else {
    discounts.value = []
  }
}

const autoSelectBestVoucherForTab = (tab) => {
  if (!tab || !Array.isArray(tab.cart)) return

  const cartTotal = getCartTotalByItems(tab.cart)
  const activeVouchers = allActiveVouchers.value.filter(isVoucherActiveNow)

  if (cartTotal <= 0 || activeVouchers.length === 0) {
    tab.discounts = []
    return
  }

  const eligibleVouchers = activeVouchers
    .map(v => ({
      voucher: v,
      discountAmount: calculateVoucherDiscount(v, cartTotal)
    }))
    .filter(item => item.discountAmount > 0)

  const bestDiscount = Math.max(...eligibleVouchers.map(item => item.discountAmount), 0)
  const bestCandidates = eligibleVouchers
    .filter(item => item.discountAmount === bestDiscount)
    .map(item => item.voucher)

  const bestVoucher = pickRandomItem(bestCandidates)
  tab.discounts = bestVoucher ? [toAppliedVoucher(bestVoucher)] : []
}

const syncAllTabsVouchers = () => {
  orderTabs.value.forEach(tab => {
    if (!Array.isArray(tab.discounts)) {
      tab.discounts = []
    }
    autoSelectBestVoucherForTab(tab)
  })
}

const syncCartPriceWithServer = async () => {
  if (!Array.isArray(orderTabs.value) || orderTabs.value.length === 0) return

  try {
    const res = await getChiTietSanPham({
      page: 0,
      size: 1000
    })

    const latestPriceMap = new Map(
      (res?.data?.content || []).map(p => [p.id, Number(p.giaBan || 0)])
    )

    orderTabs.value.forEach(tab => {
      if (!Array.isArray(tab.cart)) return

      tab.cart.forEach(item => {
        const latestPrice = latestPriceMap.get(item.id)
        if (!Number.isFinite(latestPrice)) return

        const oldPrice = Number(item.price || 0)
        if (oldPrice !== latestPrice) {
          item.priceChangeMeta = {
            oldPrice,
            newPrice: latestPrice,
            changedAt: Date.now()
          }
          item.price = latestPrice
        }
      })
    })

    // Giá thay đổi có thể làm thay đổi mức giảm tốt nhất, đồng bộ lại voucher cho tất cả tab.
    syncAllTabsVouchers()
  } catch (e) {
    console.error('Không thể đồng bộ giá sản phẩm mới nhất:', e)
  }
}

// Gợi ý mua thêm để áp dụng phiếu tốt hơn
const voucherSuggestion = computed(() => {
  const cartTotal = totalProductPrice.value
  const activeVouchers = allActiveVouchers.value.filter(isVoucherActiveNow)
  if (cartTotal <= 0 || activeVouchers.length === 0) return null

  // Tính giảm giá hiện tại
  let currentBestDiscount = 0
  discounts.value.forEach(d => {
    currentBestDiscount += calculateVoucherDiscount(d, cartTotal)
  })

  // Đề xuất theo bậc tăng dần: chọn mốc đơn tối thiểu gần nhất đang cao hơn giỏ hiện tại.
  const betterVouchers = activeVouchers
    .filter(v => v.soLuong > 0 && Number(v.minOrder || 0) > cartTotal)
    .map(v => {
      const nextTotal = Number(v.minOrder || 0)
      const potentialDiscount = calculateVoucherDiscount(v, nextTotal)
      return {
        voucher: v,
        additionalNeeded: nextTotal - cartTotal,
        potentialDiscount,
        benefit: potentialDiscount - currentBestDiscount,
        minOrder: nextTotal
      }
    })
    .filter(item => item.benefit > 0)

  if (betterVouchers.length === 0) return null

  const nextMinOrder = Math.min(...betterVouchers.map(item => item.minOrder))
  const nextTier = betterVouchers.filter(item => item.minOrder === nextMinOrder)
  const bestTierDiscount = Math.max(...nextTier.map(item => item.potentialDiscount), 0)
  const bestTierCandidates = nextTier.filter(item => item.potentialDiscount === bestTierDiscount)

  return pickRandomItem(bestTierCandidates)
})

// Polling để phát hiện phiếu giảm giá mới từ admin
const checkForBetterVouchers = async () => {
  const hasAnyCartItems = orderTabs.value.some(tab => Array.isArray(tab.cart) && tab.cart.length > 0)
  if (!hasAnyCartItems) return

  const prevIds = new Set(allActiveVouchers.value.map(v => v.id))
  const hasCoreChanges = await loadAllActiveVouchers()

  if (hasCoreChanges) {
    syncAppliedDiscountWithActiveVouchers()
    syncAllTabsVouchers()
  }

  const newVouchers = allActiveVouchers.value.filter(v => !prevIds.has(v.id))
  if (newVouchers.length === 0) return

  // Nếu chưa áp dụng phiếu nào, tự động chọn
  if (discounts.value.length === 0) {
    autoSelectBestVoucher()
    return
  }

  // Kiểm tra phiếu mới có tốt hơn không
  const cartTotal = totalProductPrice.value
  const currentDisc = totalDiscount.value

  for (const v of newVouchers) {
    const newDiscAmount = calculateVoucherDiscount(v, cartTotal)
    if (newDiscAmount > currentDisc) {
      newBetterVoucher.value = {
        ...v,
        calculatedDiscount: newDiscAmount,
        currentDiscount: currentDisc
      }
      showNewVoucherModal.value = true
      break
    }
  }
}

const applyNewVoucher = () => {
  if (newBetterVoucher.value) {
    const v = newBetterVoucher.value
    discounts.value = [{
      id: v.id,
      code: v.code,
      name: v.name,
      type: v.type,
      value: v.value,
      maxDiscount: v.maxDiscount,
      minOrder: v.minOrder,
      startDate: v.startDate,
      endDate: v.endDate
    }]
  }
  showNewVoucherModal.value = false
  newBetterVoucher.value = null
}

const keepOldVoucher = () => {
  showNewVoucherModal.value = false
  newBetterVoucher.value = null
}

/* ================= TÍNH TIỀN ================= */
const totalProductPrice = computed(() =>
  cart.value.reduce((s, i) => s + i.price * i.qty, 0)
)

const totalDiscount = computed(() => {
  let discount = 0
  const cartTotal = totalProductPrice.value

  discounts.value.forEach(d => {
    if (d.minOrder && cartTotal < d.minOrder) return

    if (d.type === 'percent') {
      let dDiscount = cartTotal * d.value / 100
      if (d.maxDiscount && d.maxDiscount > 0) {
        dDiscount = Math.min(dDiscount, d.maxDiscount)
      }
      discount += dDiscount
    } else {
      discount += d.value
    }
  })

  return Math.min(discount, cartTotal)
})

const voucherAppliedNotice = computed(() => {
  if (!Array.isArray(discounts.value) || discounts.value.length === 0) return ''

  const primaryVoucher = discounts.value[0]
  if (!primaryVoucher) return ''

  const discountAmount = calculateVoucherDiscount(primaryVoucher, totalProductPrice.value)
  if (!Number.isFinite(discountAmount) || discountAmount <= 0) return ''

  const voucherLabel = primaryVoucher.code || primaryVoucher.name || 'voucher'
  return `Áp dụng phiếu giảm giá thành công ${voucherLabel} - Giảm ${formatMoney(discountAmount)}`
})

const appliedVoucherValue = computed(() => {
  const primaryVoucher = discounts.value?.[0]
  if (!primaryVoucher) return '0'

  if (primaryVoucher.type === 'percent') {
    return `${primaryVoucher.value || 0}%`
  }

  return formatMoney(primaryVoucher.value || 0)
})

const totalPrice = computed(() =>
  Math.max(
    totalProductPrice.value - totalDiscount.value + shippingFee.value,
    0
  )
)

/* ================= THANH TOÁN ================= */
const handleCreateOrder = async () => {
  if (!cart.value.length) {
    alert('Giỏ hàng đang trống!')
    return
  }

  if (!staff.value.id) {
    alert('Vui lòng chọn nhân viên!')
    return
  }

  if (!currentOrder.value?.maHoaDon) {
    alert('Không tìm thấy mã hóa đơn nháp. Vui lòng tạo lại đơn hàng.')
    return
  }

  if (hasInvalidCartQty()) {
    alert('Số lượng sản phẩm không hợp lệ (âm hoặc vượt tồn kho). Vui lòng kiểm tra lại!')
    return
  }

  if (!confirm('Xác nhận tạo hóa đơn?')) return

  try {
    const isGuest = !customer.value.name || customer.value.name.trim() === '' || customer.value.name === 'Khách lẻ'
    const customerName = isGuest ? 'Khách lẻ' : customer.value.name
    const customerPhone = isGuest ? null : (customer.value.phone || null)
    const customerEmail = isGuest ? null : (customer.value.email || null)

    const payload = {
      maHoaDon: currentOrder.value.maHoaDon,
      tenKhachHang: customerName,
      soDienThoai: customerPhone,
      diaChi: customer.value.address,
      email: customerEmail,
      idNhanVien: staff.value.id,
      tienGiamGia: totalDiscount.value,

      phieuGiamGia: discounts.value.map(d => ({
        id: d.id,
        loaiPhieu: d.type === 'percent' ? 'PhanTram' : 'TienMat',
        giaTriGiam: d.value,
        giaTriGiamToiDa: d.maxDiscount || 0,
        donHangToiThieu: d.minOrder || 0,
        trangThai: 1
      })),

      hinhThucThanhToan:
        paymentMethod.value === 'CASH'
          ? 'TIEN_MAT'
          : 'CHUYEN_KHOAN',

      items: cart.value.map(i => ({
        idChiTietSanPham: i.id,
        soLuong: i.qty,
        donGia: i.price
      }))
    }

    await createOrder(payload)

    alert('Tạo hóa đơn thành công!')

    const tabIndex = orderTabs.value.findIndex(t => t.id === activeTabId.value)
    if (tabIndex !== -1) {
      orderTabs.value.splice(tabIndex, 1)
      activeTabId.value = orderTabs.value.length > 0 ? orderTabs.value[0].id : null
    }

    if (orderTabs.value.length === 0) {
      clearOrderTabs()
    }

    router.push({ name: orderListRouteName.value })
  } catch (err) {
    console.error(err)
    alert('Lỗi khi tạo hóa đơn!')
  }
}

/* ================= FORMAT ================= */
const formatMoney = (val) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(val)

/* ================= PAYMENT ================= */
const showPaymentModal = ref(false)
const paymentMethod = ref('TRANSFER') // TRANSFER | CASH
const customerCash = ref(0)

const openPaymentModal = () => {
  if (!cart.value.length) {
    alert('Giỏ hàng đang trống!')
    return
  }

  showPaymentModal.value = true
}

const calculateRemaining = computed(() => {
  if (paymentMethod.value === 'CASH') {
    return Math.max(0, totalPrice.value - customerCash.value)
  }
  return 0
})

const confirmCreateOrder = async () => {
  if (paymentMethod.value === 'CASH' && customerCash.value < totalPrice.value) {
    alert('Tiền khách đưa chưa đủ!')
    return
  }

  showPaymentModal.value = false
  await handleCreateOrder()
}
const qrImageUrl = computed(() => {
  const bank = '970422'
  const account = '123456789'
  const name = 'NGUYEN VAN A'
  const amount = totalPrice.value

  return `https://img.vietqr.io/image/${bank}-${account}-compact.png?amount=${amount}&addInfo=THANH TOAN HOA DON&accountName=${encodeURIComponent(name)}`
})

/* ================= STAFF ================= */
const showStaffModal = ref(false)
const staffKeyword = ref('')
import { getNhanVien } from '@/api/NhanVienApi'

const staffs = ref([])
const staffPage = ref(0)
const staffSize = ref(10)
const totalStaffPages = ref(0)

const loadStaffs = async () => {
  const res = await getNhanVien({
    page: staffPage.value,
    size: staffSize.value,
    status: 1
  })

  staffs.value = res.data.content.map(s => ({
    id: s.id,
    code: s.maNhanVien,
    name: s.tenNhanVien
  }))

  totalStaffPages.value = res.data.totalPages
}

const filteredStaffs = computed(() =>
  staffs.value.filter(s =>
    [s.code, s.name]
      .join(' ')
      .toLowerCase()
      .includes(staffKeyword.value.toLowerCase())
  )
)

const openStaffModal = async () => {
  staffPage.value = 0
  await loadStaffs()
  showStaffModal.value = true
}
const selectStaff = (s) => {
  staff.value = {
    id: s.id,
    code: s.code,
    name: s.name
  }
  showStaffModal.value = false
}

import { createOrderDelivery } from '@/api/HoaDonApi'
import { ghnApi } from '@/api/ghnApi'

const handleSubmitOrder = async () => {
  if (orderType.value === 'TAI_QUAY') {
    openPaymentModal()
  } else {
    await handleCreateOrderDelivery()
  }
}

const handleCreateOrderDelivery = async () => {
  if (!cart.value.length) {
    alert('Giỏ hàng đang trống!')
    return
  }

  const customerName = (customer.value?.name || '').trim()
  const customerPhone = (customer.value?.phone || '').trim()
  const customerEmail = (customer.value?.email || '').trim()
  const detailAddress = (customer.value?.address || '').trim()
  const recipientName = (recipient.value?.name || '').trim()
  const recipientPhone = (recipient.value?.phone || '').trim()

  if (!customerName || !customerPhone || !customerEmail) {
    alert('Vui lòng nhập đầy đủ thông tin khách hàng')
    return
  }

  if (!recipientName) {
    alert('Vui lòng nhập tên người nhận')
    return
  }

  if (!recipientPhone) {
    alert('Vui lòng nhập SĐT người nhận')
    return
  }

  if (!selectedProvince.value || !selectedDistrict.value || !selectedWard.value) {
    alert('Vui lòng chọn đầy đủ địa chỉ giao hàng')
    return
  }

  if (!detailAddress) {
    alert('Vui lòng nhập địa chỉ cụ thể')
    return
  }

  if (!staff.value.id) {
    alert('Vui lòng chọn nhân viên!')
    return
  }

  if (hasInvalidCartQty()) {
    alert('Số lượng sản phẩm không hợp lệ (âm hoặc vượt tồn kho). Vui lòng kiểm tra lại!')
    return
  }

  const province = provinces.value.find(
    p => p.provinceId == selectedProvince.value
  )

  const district = districts.value.find(
    d => d.districtId == selectedDistrict.value
  )

  const ward = wards.value.find(
    w => w.wardCode == selectedWard.value
  )

  const payload = {
    tenKhachHang: recipientName,
    soDienThoai: recipientPhone,

    diaChiChiTiet: detailAddress,

    provinceName: province?.provinceName || '',
    districtName: district?.districtName || '',
    wardName: ward?.wardName || '',

    email: customerEmail,
    ghiChu: note.value,
    idNhanVien: staff.value.id,

    tienGiamGia: totalDiscount.value,
    phiVanChuyen: shippingFee.value,

    phieuGiamGia: discounts.value.map(d => ({
      id: d.id,
      loaiPhieu: d.type === 'percent' ? 'PhanTram' : 'TienMat',
      giaTriGiam: d.value,
      giaTriGiamToiDa: d.maxDiscount || 0,
      donHangToiThieu: d.minOrder || 0,
      trangThai: 1
    })),

    items: cart.value.map(i => ({
      idChiTietSanPham: i.id,
      soLuong: i.qty,
      donGia: i.price
    }))
  }
  await createOrderDelivery(payload)

  if (currentOrder.value?.maHoaDon) {
    try {
      await deletePosDraftOrder(currentOrder.value.maHoaDon)
    } catch (error) {
      console.error(error)
    }
  }

  alert('Tạo đơn giao hàng thành công – chờ xác nhận')

  const tabIndex = orderTabs.value.findIndex(t => t.id === activeTabId.value)
  if (tabIndex !== -1) {
    orderTabs.value.splice(tabIndex, 1)
    activeTabId.value = orderTabs.value.length > 0 ? orderTabs.value[0].id : null
  }

  if (orderTabs.value.length === 0) {
    clearOrderTabs()
  }

  router.push({ name: orderListRouteName.value })
}

const MAX_TABS = 5

const orderTabs = ref([])
const activeTabId = ref(null)
const nextOrderNumber = ref(1)

/* ================= LOCALSTORAGE PERSISTENCE ================= */
const getStorageKey = () => {
  const user = getCurrentUser()
  const id = user?.id || 'unknown'
  const role = getRole() || 'none'
  return `pos_order_tabs_${role}_${id}`
}
const STORAGE_KEY = getStorageKey()
const currentDateKey = ref('')
let dailyResetTimer = null

const getLocalDateKey = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const getTabItemCount = (tab) => {
  if (!tab?.cart?.length) return 0
  return tab.cart.reduce((sum, item) => sum + (Number(item.qty) || 0), 0)
}

const getTabLabel = (tab) => {
  if (tab?.maHoaDon) return tab.maHoaDon
  if (tab?.tabName) return tab.tabName
  if (Number.isFinite(tab?.orderNumber)) return `Đơn ${tab.orderNumber}`
  return 'Đơn mới'
}

const saveOrderTabs = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      orderTabs: orderTabs.value,
      activeTabId: activeTabId.value,
      nextOrderNumber: nextOrderNumber.value,
      savedDate: getLocalDateKey()
    }))
  } catch (err) {
    console.error('Lỗi khi lưu dữ liệu:', err)
  }
}

const resetExpiredPendingOrders = async (tabs) => {
  for (const tab of tabs || []) {
    if (!Array.isArray(tab.cart) || tab.cart.length === 0) continue

    for (const item of tab.cart) {
      const qtyToRelease = Number(item.qty || 0)
      if (qtyToRelease <= 0) continue
      await releaseProductStock(item.id, qtyToRelease, false)
    }
  }
}

const loadOrderTabs = async () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (!stored) return

    const data = JSON.parse(stored)
    const savedDate = data?.savedDate
    const today = getLocalDateKey()

    if (savedDate && savedDate !== today) {
      await resetExpiredPendingOrders(data.orderTabs || [])
      clearOrderTabs()
      orderTabs.value = []
      activeTabId.value = null
      nextOrderNumber.value = 1
      return
    }

    orderTabs.value = Array.isArray(data?.orderTabs) ? data.orderTabs : []
    activeTabId.value = data?.activeTabId || null

    let maxOrderNumber = 0
    orderTabs.value.forEach((tab, index) => {
      if (!Number.isFinite(tab.orderNumber) || tab.orderNumber <= 0) {
        tab.orderNumber = index + 1
      }
      if (!tab.tabName) {
        tab.tabName = `Đơn ${tab.orderNumber}`
      }
      if (!tab.customer || typeof tab.customer !== 'object') {
        tab.customer = { name: '', phone: '', email: '', address: '' }
      } else {
        tab.customer.name = tab.customer.name || ''
        tab.customer.phone = tab.customer.phone || ''
        tab.customer.email = tab.customer.email || ''
        tab.customer.address = tab.customer.address || ''
      }
      if (!tab.recipient) {
        tab.recipient = { name: '', phone: '' }
      }
      maxOrderNumber = Math.max(maxOrderNumber, tab.orderNumber)
    })

    const savedNextOrderNumber = Number(data?.nextOrderNumber)
    if (Number.isFinite(savedNextOrderNumber) && savedNextOrderNumber > maxOrderNumber) {
      nextOrderNumber.value = savedNextOrderNumber
    } else {
      nextOrderNumber.value = maxOrderNumber + 1
    }

    if (orderTabs.value.length > 0) {
      const hasActiveTab = orderTabs.value.some(tab => tab.id === activeTabId.value)
      if (!hasActiveTab) {
        activeTabId.value = orderTabs.value[orderTabs.value.length - 1].id
      }
    } else {
      activeTabId.value = null
    }
  } catch (err) {
    console.error('Lỗi khi tải dữ liệu:', err)
  }
}

const clearOrderTabs = () => {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch (err) {
    console.error('Lỗi khi xóa dữ liệu:', err)
  }
}

const runDailyResetIfNeeded = async () => {
  const today = getLocalDateKey()
  if (today === currentDateKey.value) return

  if (orderTabs.value.length > 0) {
    await resetExpiredPendingOrders(orderTabs.value)
  }

  orderTabs.value = []
  activeTabId.value = null
  nextOrderNumber.value = 1
  clearOrderTabs()
  currentDateKey.value = today

  alert('Đã sang ngày mới, hệ thống đã xóa toàn bộ hóa đơn chờ và hoàn tồn kho.')
}

onMounted(async () => {
  await loadOrderTabs()
  currentDateKey.value = getLocalDateKey()

  dailyResetTimer = window.setInterval(() => {
    void runDailyResetIfNeeded()
  }, 60000)

  fetchProvinces() // Load danh sách tỉnh/thành

  // Tải phiếu giảm giá và tự động chọn tốt nhất
  await loadAllActiveVouchers()
  syncAppliedDiscountWithActiveVouchers()
  autoSelectBestVoucher()
  await syncCartPriceWithServer()

  // Polling nhanh để cập nhật gần realtime khi voucher đổi số lượng/% giảm/đơn tối thiểu.
  voucherPollTimer = window.setInterval(() => {
    void checkForBetterVouchers()
  }, VOUCHER_POLL_INTERVAL_MS)

  // Polling giá sản phẩm mỗi 20 giây để cập nhật khi giá bị chỉnh sửa
  pricePollTimer = window.setInterval(() => {
    void syncCartPriceWithServer()
  }, 20000)
})

onUnmounted(() => {
  if (dailyResetTimer) {
    window.clearInterval(dailyResetTimer)
    dailyResetTimer = null
  }
  if (voucherPollTimer) {
    window.clearInterval(voucherPollTimer)
    voucherPollTimer = null
  }
  if (pricePollTimer) {
    window.clearInterval(pricePollTimer)
    pricePollTimer = null
  }
})

watch(orderTabs, saveOrderTabs, { deep: true })
watch(activeTabId, saveOrderTabs)

watch(staffPage, loadStaffs)
watch(productPage, loadProducts)
watch(customerPage, loadCustomers)
watch(discountPage, loadDiscounts)

const createNewTab = async () => {
  if (orderTabs.value.length >= MAX_TABS) {
    alert('Chỉ được tối đa 5 đơn hàng')
    return
  }

  // Lấy thông tin nhân viên hiện tại (admin hoặc staff đều là nhân viên)
  const currentUser = getCurrentUser()
  let staffInfo = { id: null, code: '', name: '' }

  if (currentUser) {
    staffInfo = {
      id: currentUser.id,
      code: currentUser.maNhanVien || '',
      name: currentUser.tenNhanVien || ''
    }
  }

  let maHoaDon = null
  try {
    const res = await createPosDraftOrder({ idNhanVien: staffInfo.id || null })
    maHoaDon = res?.data?.maHoaDon || null
  } catch (error) {
    console.error(error)
    alert('Không thể tạo hóa đơn nháp. Vui lòng thử lại!')
    return
  }

  const newTab = {
    id: Date.now(),
    orderNumber: nextOrderNumber.value,
    tabName: maHoaDon || `Đơn ${nextOrderNumber.value}`,
    maHoaDon,
    orderType: 'TAI_QUAY',
    cart: [],
    customer: { name: '', phone: '', email: '', address: '' },
    recipient: { name: '', phone: '' },
    staff: staffInfo,
    discounts: [],
    shippingFee: 0,
    note: ''
  }

  nextOrderNumber.value += 1
  orderTabs.value.push(newTab)
  activeTabId.value = newTab.id
}

const releaseTabStock = async (tab) => {
  for (const item of tab.cart) {
    const qtyToRelease = Number(item.qty || 0)
    if (qtyToRelease <= 0) continue

    const released = await releaseProductStock(item.id, qtyToRelease)
    if (!released) {
      return false
    }
  }

  return true
}

const closeTab = async (id) => {
  const index = orderTabs.value.findIndex(t => t.id === id)
  if (index === -1) return

  const tab = orderTabs.value[index]

  if (getTabItemCount(tab) > 0) {
    const accepted = confirm('Đơn hàng này đã chứa sản phẩm. Bạn có đồng ý xóa không?')
    if (!accepted) return

    const released = await releaseTabStock(tab)
    if (!released) {
      alert('Không thể hoàn kho cho tab này. Vui lòng thử lại!')
      return
    }
  }

  if (tab.maHoaDon) {
    try {
      await deletePosDraftOrder(tab.maHoaDon)
    } catch (error) {
      console.error(error)
      alert('Không thể xóa hóa đơn nháp trong hệ thống. Vui lòng thử lại!')
      return
    }
  }

  orderTabs.value.splice(index, 1)

  if (activeTabId.value === id) {
    activeTabId.value = orderTabs.value.length
      ? orderTabs.value[orderTabs.value.length - 1].id
      : null
  }
}

const currentOrder = computed(() =>
  orderTabs.value.find(t => t.id === activeTabId.value)
)

const cart = computed({
  get: () => currentOrder.value?.cart || [],
  set: v => currentOrder.value && (currentOrder.value.cart = v)
})

const customer = computed({
  get: () => {
    if (!currentOrder.value) {
      return { name: '', phone: '', email: '', address: '' }
    }

    if (!currentOrder.value.customer || typeof currentOrder.value.customer !== 'object') {
      currentOrder.value.customer = { name: '', phone: '', email: '', address: '' }
    }

    return currentOrder.value.customer
  },
  set: v => currentOrder.value && (currentOrder.value.customer = v)
})

const recipient = computed({
  get: () => currentOrder.value?.recipient || {},
  set: v => currentOrder.value && (currentOrder.value.recipient = v)
})

const staff = computed({
  get: () => currentOrder.value?.staff || {},
  set: v => currentOrder.value && (currentOrder.value.staff = v)
})

const discounts = computed({
  get: () => currentOrder.value?.discounts || [],
  set: v => currentOrder.value && (currentOrder.value.discounts = v)
})

const shippingFee = computed({
  get: () => currentOrder.value?.shippingFee ?? 0,
  set: v => currentOrder.value && (currentOrder.value.shippingFee = Number(v) || 0)
})

const note = computed({
  get: () => currentOrder.value?.note || '',
  set: v => currentOrder.value && (currentOrder.value.note = v)
})


const orderType = computed({
  get: () => currentOrder.value?.orderType || 'TAI_QUAY',
  set: v => {
    if (currentOrder.value) {
      currentOrder.value.orderType = v
    }
  }
})

const isDelivery = computed({
  get: () => orderType.value === 'GIAO_HANG',
  set: v => {
    orderType.value = v ? 'GIAO_HANG' : 'TAI_QUAY'
    if (!v) {
      shippingFee.value = 0
    }
  }
})

watch(orderType, (value) => {
  if (value === 'TAI_QUAY') {
    clearDeliveryFormWhenBackToCounter()
  }
})

const syncRecipientFromCustomer = () => {
  if (!currentOrder.value || orderType.value !== 'GIAO_HANG') {
    return
  }

  recipient.value = {
    ...(recipient.value || {}),
    name: customer.value?.name || '',
    phone: customer.value?.phone || ''
  }
}

watch(
  () => [customer.value?.name, customer.value?.phone, orderType.value],
  () => {
    syncRecipientFromCustomer()
  }
)

// Tự động chọn phiếu giảm giá tốt nhất khi tổng giỏ hàng thay đổi
watch(totalProductPrice, async () => {
  if (allActiveVouchers.value.length === 0) {
    await loadAllActiveVouchers()
    syncAppliedDiscountWithActiveVouchers()
  }
  syncAllTabsVouchers()
})

watch(activeTabId, () => {
  void syncCartPriceWithServer()
  void checkForBetterVouchers()
})

const toggleOrderType = () => {
  orderType.value =
    orderType.value === 'TAI_QUAY'
      ? 'GIAO_HANG'
      : 'TAI_QUAY'
}

// GHN Address variables
const provinces = ref([])
const districts = ref([])
const wards = ref([])
const selectedProvince = ref('')
const selectedDistrict = ref('')
const selectedWard = ref('')

const isDeliveryAddressReady = computed(() =>
  Boolean(selectedProvince.value && selectedDistrict.value && selectedWard.value)
)

const refreshShippingFee = async () => {
  if (orderType.value !== 'GIAO_HANG') {
    shippingFee.value = 0
    return
  }

  if (!isDeliveryAddressReady.value) {
    shippingFee.value = 0
    return
  }

  await calculateShippingFee()
}

const clearDeliveryFormWhenBackToCounter = () => {
  customer.value = {
    name: '',
    phone: '',
    email: '',
    address: ''
  }

  recipient.value = {
    name: '',
    phone: ''
  }

  note.value = ''
  selectedProvince.value = ''
  selectedDistrict.value = ''
  selectedWard.value = ''
  districts.value = []
  wards.value = []
  shippingFee.value = 0
}

// GHN Functions
const fetchProvinces = async () => {
  try {
    const res = await ghnApi.getProvinces()

    console.log("PROVINCES =", res.data)

    provinces.value = res.data
  } catch (error) {
    console.error("Lỗi tải tỉnh:", error)
  }
}

const onProvinceChange = async () => {
  selectedDistrict.value = ''
  selectedWard.value = ''
  districts.value = []
  wards.value = []
  shippingFee.value = 0

  if (selectedProvince.value) {
    try {
      const res = await ghnApi.getDistricts(selectedProvince.value)
      districts.value = res.data
    } catch (error) {
      console.error('Lỗi tải danh sách quận/huyện:', error)
    }
  }
}

const onDistrictChange = async () => {
  selectedWard.value = ''
  wards.value = []
  shippingFee.value = 0

  if (selectedDistrict.value) {
    try {
      const res = await ghnApi.getWards(selectedDistrict.value)
      wards.value = res.data
    } catch (error) {
      console.error('Lỗi tải danh sách phường/xã:', error)
    }
  }
}

const onWardChange = async () => {
  if (selectedProvince.value && selectedDistrict.value && selectedWard.value) {
    await calculateShippingFee()
  } else {
    shippingFee.value = 0
  }
}

const calculateShippingFee = async () => {
  try {
    const requestData = {
      fromDistrictId: 1442, // ID quận của shop (có thể lấy từ API)
      fromWardCode: '21012', // Ward code của shop
      toDistrictId: parseInt(selectedDistrict.value),
      toWardCode: selectedWard.value,
      weight: 1000, // Trọng lượng mặc định (gram)
      length: 30, // Kích thước mặc định (cm)
      width: 20,
      height: 10
    }

    const res = await ghnApi.calculateShippingFee(requestData)
    shippingFee.value = res.data.total || 0
  } catch (error) {
    console.error('Lỗi tính phí vận chuyển:', error)
    shippingFee.value = 0
  }
}

const getFullAddress = () => {
  const province = provinces.value.find(p => p.ProvinceID == selectedProvince.value)
  const district = districts.value.find(d => d.DistrictID == selectedDistrict.value)
  const ward = wards.value.find(w => w.WardCode == selectedWard.value)

  const provinceName = province ? province.ProvinceName : ''
  const districtName = district ? district.DistrictName : ''
  const wardName = ward ? ward.WardName : ''

  return `${wardName}, ${districtName}, ${provinceName}`.trim()
}
</script>

<style scoped>
/* ===========================
   ✅ SCROLL FIX TRIỆT ĐỂ
   =========================== */

/* ĐỪNG đặt overflow:auto ở pos-page (nó tạo container scroll riêng + dễ bị cắt trong layout cha) */
.pos-page {
  min-height: 100vh;
}

/* nếu layout cha có overflow hidden, cái này giúp body vẫn scroll */
:global(html),
:global(body) {
  height: 100%;
  overflow: auto;
}

/* ✅ tạo “khung” cho POS: 2 cột cuộn riêng */
.pos-main-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto auto;
  gap: 12px;
  margin-top: 12px;

  /* cho phép item co giãn trong grid */
  min-height: 0;

  /* chiều cao làm việc: auto để hiển thị hết nội dung */
  height: auto;
}

/* giữ vị trí */
.pos-main-container>.order-type-tabs {
  grid-column: 1 / -1;
}

/* ✅ Sản phẩm FULL WIDTH hàng 1 */
.pos-main-container>.card:first-child {
  grid-column: 1 / -1;
  grid-row: 1;
  overflow-x: hidden;
}

/* Khách hàng - bên trái hàng 2 */
.pos-main-container>.pos-cart {
  grid-column: 1;
  grid-row: 2;
  overflow-x: hidden;
}

/* Info - bên phải hàng 2 */
.pos-main-container>.pos-info {
  grid-column: 2;
  grid-row: 2;
  overflow-x: hidden;
}

/* Sticky cho tabs loại đơn trong khung cuộn */
.order-type-tabs {
  position: sticky;
  top: 0;
  z-index: 5;
}

/* Mobile: không cuộn riêng, cuộn theo trang */
@media (max-width: 1100px) {
  .pos-main-container {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto;
    height: auto;
  }

  /* Sản phẩm full width hàng 1 */
  .pos-main-container>.card:first-child {
    grid-column: 1;
    grid-row: 1;
    overflow: visible;
  }

  /* Khách hàng full width hàng 2 */
  .pos-main-container>.pos-cart {
    grid-column: 1;
    grid-row: 2;
    overflow: visible;
  }

  /* Info full width hàng 3 */
  .pos-main-container>.pos-info {
    grid-column: 1;
    grid-row: 3;
    overflow: visible;
  }
}

/* ===========================
   PHẦN CSS UI (giữ nguyên của bạn)
   =========================== */

.muted {
  color: #64748b;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  white-space: nowrap;
}

.mt {
  margin-top: 12px;
}

/* ===== TOP BAR ===== */
.pos-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 14px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(2, 6, 23, 0.06);
}

.pos-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pos-badge {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #1d4ed8;
  background: linear-gradient(180deg, #eff6ff, #ffffff);
  border: 1px solid #dbeafe;
}

.pos-title h2 {
  margin: 0;
  font-size: 18px;
  line-height: 1.2;
}

.pos-sub {
  margin: 2px 0 0;
  font-size: 13px;
  color: #64748b;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tab-counter {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 13px;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #cbd5e1;
  box-shadow: 0 0 0 4px rgba(203, 213, 225, 0.25);
}

.dot.on {
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.20);
}

/* ===== BUTTONS ===== */
.btn-primary {
  border: none;
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.22);
  transition: transform .12s ease, box-shadow .12s ease, filter .12s ease;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.btn-primary:hover {
  transform: translateY(-1px);
  filter: brightness(1.02);
}

.btn-primary:active {
  transform: translateY(0px);
  box-shadow: 0 8px 14px rgba(37, 99, 235, 0.18);
}

.btn-primary.big {
  padding: 12px 16px;
  font-size: 14px;
  border-radius: 14px;
}

.btn-icon {
  font-size: 18px;
  line-height: 1;
}

.btn-outline {
  background: #fff;
  border: 1px solid #c7d2fe;
  color: #1d4ed8;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background .12s ease, transform .12s ease;
}

.btn-outline:hover {
  background: #eff6ff;
  transform: translateY(-1px);
}

/* ===== ORDER TABS ===== */
.order-tabs {
  display: flex;
  gap: 10px;
  padding: 12px 4px 0;
  flex-wrap: wrap;
  align-items: center;
}

.order-tab {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: transform .12s ease, box-shadow .12s ease, border-color .12s ease;
  box-shadow: 0 8px 18px rgba(2, 6, 23, 0.05);
}

.order-tab:hover {
  transform: translateY(-1px);
  border-color: #c7d2fe;
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.07);
}

.order-tab.active {
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.98), rgba(29, 78, 216, 0.98));
  border-color: rgba(37, 99, 235, 0.55);
  color: #fff;
}

.order-tab.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tab-code {
  font-size: 13px;
  font-weight: 800;
}

.tab-meta {
  font-size: 12px;
  opacity: 0.9;
}

.close-tab {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
  display: grid;
  place-items: center;
}

.order-tab.active .close-tab {
  border-color: rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

/* ===== EMPTY STATE ===== */
.empty-shell {
  padding-top: 14px;
}

.empty-card {
  border-radius: 18px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  box-shadow: 0 14px 34px rgba(2, 6, 23, 0.07);
  overflow: hidden;
}

.empty-hero {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 14px;
  padding: 18px;
  align-items: center;
}

.hero-left h3 {
  margin: 10px 0 6px;
  font-size: 20px;
}

.hero-left p {
  margin: 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  max-width: 560px;
}

.hero-icon {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  color: #1d4ed8;
  background: linear-gradient(180deg, #eff6ff, #ffffff);
  border: 1px solid #dbeafe;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.12);
}

.empty-actions {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hint {
  font-size: 13px;
  color: #64748b;
}

.hero-right {
  display: flex;
  justify-content: flex-end;
}

.mock {
  width: min(360px, 100%);
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
  overflow: hidden;
}

.mock-top {
  display: flex;
  gap: 6px;
  padding: 10px 12px;
  border-bottom: 1px solid #e2e8f0;
}

.mock-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #e2e8f0;
}

.mock-body {
  padding: 12px;
}

.mock-row {
  height: 10px;
  border-radius: 999px;
  background: #e2e8f0;
  margin-bottom: 10px;
}

.w-80 {
  width: 80%;
}

.w-70 {
  width: 70%;
}

.w-60 {
  width: 60%;
}

.w-50 {
  width: 50%;
}

.mock-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 14px 0;
}

.mock-kpi {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 14px;
}

.kpi {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #fff;
}

.kpi-label {
  font-size: 12px;
  color: #64748b;
}

.kpi-val {
  font-size: 14px;
  font-weight: 800;
  color: #dc2626;
  margin-top: 2px;
}

.mock-btn {
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  opacity: 0.35;
}

.empty-steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 14px 18px 18px;
  border-top: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(248, 250, 252, 0.7);
}

.step {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.8);
}

.step-no {
  width: 30px;
  height: 30px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 900;
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px solid #dbeafe;
}

.step-text {
  font-size: 13px;
}

.step-text b {
  display: block;
  margin-bottom: 2px;
}

.step-text .muted {
  font-size: 12px;
}

@media (max-width: 980px) {
  .empty-hero {
    grid-template-columns: 1fr;
  }

  .hero-right {
    justify-content: flex-start;
  }

  .empty-steps {
    grid-template-columns: 1fr;
  }
}

/* ===== ORDER TYPE TABS ===== */
.order-type-tabs {
  display: flex;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 24px rgba(2, 6, 23, 0.05);
}

.tab-item {
  flex: none;
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  cursor: pointer;
  font-weight: 800;
  font-size: 13px;
  transition: all .12s ease;
}

.tab-item.active {
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  border-color: rgba(37, 99, 235, 0.55);
  color: #fff;
}

/* ===== CARDS ===== */
.card {
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 26px rgba(2, 6, 23, 0.06);
  overflow: visible;
}

.card-header {
  padding: 14px 14px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.card-title h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
}

.card-title .muted {
  display: block;
  margin-top: 2px;
  font-size: 12px;
}

.card-body {
  padding: 14px;
}

/* chips */
.chip {
  margin-left: 10px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 12px;
  font-weight: 800;
  color: #334155;
}

/* ===== ADDRESS GROUP ===== */
.address-group {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
}

.address-field {
  display: flex;
  flex-direction: column;
}

.address-label {
  font-size: 12px;
  font-weight: 700;
  color: #374151;
  margin-bottom: 4px;
}

.address-select {
  padding: 11px 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color .12s ease, box-shadow .12s ease;
}

.address-select:focus {
  border-color: #93c5fd;
  box-shadow: 0 0 0 4px rgba(147, 197, 253, 0.25);
}

.address-select:disabled {
  background: #f8fafc;
  color: #64748b;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .address-group {
    grid-template-columns: 1fr;
  }
}

/* FIX SELECT DROPDOWN BUG */
.address-select {
  position: relative;
  z-index: 10;
  appearance: auto !important;
  -webkit-appearance: auto !important;
  -moz-appearance: auto !important;
}

.address-select option {
  display: block;
  white-space: normal;
}

/* QUAN TRỌNG */
.card,
.pos-info,
.pos-cart,
.pos-main-container {
  overflow: visible !important;
}

input,
textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color .12s ease, box-shadow .12s ease;
}

textarea {
  min-height: 92px;
  resize: vertical;
}

input:focus,
textarea:focus {
  border-color: #93c5fd;
  box-shadow: 0 0 0 4px rgba(147, 197, 253, 0.25);
}

input:disabled {
  background: #f8fafc;
  color: #64748b;
}

/* ===== TABLE ===== */
.table-wrap {
  overflow-x: auto;
}

.customer-actions {
  display: flex;
  gap: 8px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: transparent;
}

.table thead th {
  font-size: 12px;
  text-transform: none;
  letter-spacing: .2px;
  color: #475569;
  background: rgba(248, 250, 252, 0.9);
  border-bottom: 1px solid rgba(226, 232, 240, 0.95);
  padding: 10px 8px;
  text-align: center;
}

.table td {
  padding: 12px 8px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.85);
  font-size: 13px;
  vertical-align: middle;
  text-align: center;
}

.table tbody tr:hover td {
  background: rgba(239, 246, 255, 0.35);
}

.customer-inline-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.customer-inline-label {
  min-width: 150px;
  color: #6b7280;
  font-size: 14px;
}

.customer-inline-value {
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
}

.delivery-summary {
  margin-bottom: 14px;
}

.counter-note {
  font-size: 14px;
}

.delivery-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  color: #4b5563;
  font-weight: 600;
}

.field-label span,
.address-label span {
  color: #ef4444;
}

.delivery-address-group {
  margin-bottom: 12px;
}

.full-width {
  width: 100%;
}

.note-field textarea {
  min-height: 80px;
}

@media (max-width: 768px) {
  .customer-inline-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .customer-inline-label {
    min-width: unset;
  }

  .delivery-form-grid {
    grid-template-columns: 1fr;
  }

  .voucher-inline-grid {
    grid-template-columns: 1fr;
  }

  .pay-body .row {
    grid-template-columns: 1fr;
    gap: 6px;
  }
}

.name-cell {
  text-align: left;
  min-width: 180px;
}

.name-main {
  font-weight: 800;
  color: #0f172a;
}

.name-sub {
  font-size: 12px;
}

.p-price {
  color: #dc2626;
  font-weight: 900;
}

.price-main {
  color: #dc2626;
  font-weight: 900;
}

.price-old {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
  text-decoration: line-through;
  font-weight: 700;
}

.price-change-note {
  margin-top: 4px;
  text-align: left;
}

.price-change-title {
  font-size: 11px;
  color: #09003d;
  font-weight: 700;
  line-height: 1.2;
}

.price-change-flow {
  font-size: 12px;
  color: #09003d;
  font-weight: 700;
  line-height: 1.2;
}

.price-col {
  text-align: right;
  font-weight: 900;
  color: #dc2626;
}

.price-col.total {
  font-size: 18px;
}

/* ===== ITEM CONTROL ===== */
.item-control {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  padding: 6px 8px;
  border-radius: 999px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.95);
}

.item-control button {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  cursor: pointer;
  font-weight: 900;
}

.item-control button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.item-control span {
  min-width: 20px;
  text-align: center;
  font-weight: 900;
}

.qty-input {
  width: 58px;
  height: 30px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  text-align: center;
  font-weight: 700;
  color: #0f172a;
  background: #fff;
}

.qty-input-error {
  border-color: #ef4444;
}

.qty-input:focus {
  outline: none;
  border-color: #93c5fd;
  box-shadow: 0 0 0 2px rgba(147, 197, 253, 0.25);
}

.qty-warning {
  margin-top: 4px;
  font-size: 11px;
  line-height: 1.3;
  color: #dc2626;
}

.product-check {
  width: 16px;
  height: 16px;
  margin: 0;
  padding: 0;
  display: inline-block;
  vertical-align: middle;
  cursor: pointer;
  accent-color: #1d4ed8;
}

.product-check:focus {
  box-shadow: none;
}

/* ===== EMPTY CART ===== */
.empty-cart {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 44px 12px;
  border-radius: 14px;
  border: 1px dashed rgba(148, 163, 184, 0.55);
  background: rgba(248, 250, 252, 0.65);
}

.empty-icon {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: #fff;
  border: 1px solid rgba(226, 232, 240, 0.95);
  font-size: 20px;
}

.empty-text b {
  font-size: 14px;
}

.empty-small {
  font-size: 13px;
  color: #64748b;
  text-align: center;
  padding: 12px 0;
}

.voucher-apply-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-bottom: 0;
}

.voucher-code-input {
  width: 100%;
  height: 38px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
}

.voucher-code-input:focus {
  outline: none;
  border-color: #93c5fd;
  box-shadow: 0 0 0 2px rgba(147, 197, 253, 0.2);
}

.btn-apply-code {
  height: 38px;
  min-width: 82px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 12px;
  background: #f8fafc;
  color: #374151;
  font-weight: 700;
  cursor: pointer;
}

.btn-apply-code:hover {
  background: #eef2f7;
}

.voucher-applied-notice {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
  color: #15803d;
  font-size: 13px;
  font-weight: 600;
}

.merged-payment-card .pay-body {
  display: grid;
  gap: 10px;
}

.voucher-inline-grid {
  display: grid;
  grid-template-columns: 1fr 180px;
  gap: 10px;
}

.voucher-inline-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.voucher-inline-field label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 600;
}

.voucher-value-input {
  height: 38px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  color: #475569;
  background: #f1f5f9;
  font-weight: 700;
}

.voucher-suggestion-inline {
  margin: 0;
}

/* ===== PAYMENT CARD ===== */
.payment-card {
  margin-top: auto;
}

.pay-body .row {
  display: grid;
  grid-template-columns: 1fr 160px;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(226, 232, 240, 0.75);
  font-size: 13px;
}

.pay-body .row:last-of-type {
  border-bottom: none;
}

.shipping-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.shipping-logo {
  height: 18px;
  width: auto;
  object-fit: contain;
}

.shipping-control-wrap {
  display: grid;
  grid-template-columns: 1fr 34px auto;
  align-items: center;
  gap: 6px;
}

.ship-refresh-btn {
  width: 34px;
  height: 34px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background: #fff;
  color: #334155;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
}

.ship-refresh-btn:hover {
  background: #f8fafc;
}

.ship-unit {
  font-size: 13px;
  color: #6b7280;
  font-weight: 700;
}

.shipping-hint {
  border: 1px solid #f1d5d5;
  background: #fef2f2;
  color: #7f1d1d;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 13px;
}

.total-row {
  border-bottom: none !important;
  padding-top: 14px !important;
  margin-top: 6px;
  border-top: 2px dashed rgba(226, 232, 240, 0.95);
}

.ship-input {
  width: 100%;
  text-align: right;
  font-weight: 900;
  color: #dc2626;
}

.btn-pay {
  width: 100%;
  padding: 12px 14px;
  margin-top: 12px;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  font-weight: 900;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(180deg, #16a34a, #15803d);
  box-shadow: 0 12px 22px rgba(22, 163, 74, 0.22);
  transition: transform .12s ease, filter .12s ease;
}

.btn-pay:hover {
  transform: translateY(-1px);
  filter: brightness(1.02);
}

.pay-note {
  margin-top: 10px;
  font-size: 12px;
}

/* ===== REMOVE BUTTON ===== */
.btn-remove {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  border: 1px solid rgba(254, 202, 202, 0.95);
  background: rgba(255, 245, 245, 0.95);
  color: #dc2626;
  font-size: 16px;
  font-weight: 900;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all .12s ease;
}

.btn-remove:hover {
  background: #dc2626;
  color: #fff;
  border-color: #dc2626;
}

/* ===== MODAL ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content,
.payment-modal {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 18px;
  box-shadow: 0 26px 60px rgba(2, 6, 23, 0.25);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content {
  padding: 16px;
  width: 560px;
}

.modal-content.large {
  width: 900px;
  max-width: 95vw;
}

.modal-content.discount-modal {
  width: 980px;
  max-width: 95vw;
}

.modal-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.modal-header-flex h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
}

.close-btn {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: #fff;
  cursor: pointer;
  font-size: 20px;
  font-weight: 900;
  display: grid;
  place-items: center;
}

.search-input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  margin-bottom: 12px;
  outline: none;
}

.product-filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px 12px;
  margin-bottom: 12px;
}

.filter-item label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.slider-wrap {
  display: grid;
  grid-template-columns: minmax(280px, 1fr) auto 1fr auto;
  align-items: center;
  gap: 12px;
}

.slider-wrap .search-input {
  margin-bottom: 0;
}

.price-slider {
  width: 100%;
  accent-color: #0f172a;
}

.modal-table-wrapper {
  max-height: 55vh;
  overflow-y: auto;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.7);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  font-size: 13px;
}

.pagination button {
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: #fff;
  cursor: pointer;
  font-weight: 800;
}

.pagination button:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}

/* ===== PRICE RANGE ===== */
.price-range {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.price-range span {
  font-size: 13px;
  font-weight: 900;
  color: #334155;
}

.price-range input {
  width: 160px;
  padding: 9px 10px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
}

.slider-wrap input.price-slider {
  width: 100%;
  padding: 0;
  border: none;
}

@media (max-width: 1200px) {
  .product-filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .slider-wrap {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}

/* ===== MODAL TABLE ===== */
.modal-table {
  table-layout: fixed;
}

.modal-table th,
.modal-table td {
  vertical-align: middle;
}

.modal-table td:first-child,
.modal-table th:first-child {
  width: 120px;
}

.modal-content.large .modal-table .product-code-cell .mono {
  display: block;
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.35;
}

.product-code-head,
.product-code-cell {
  text-align: left;
}

.modal-content.large .modal-table th:nth-child(7),
.modal-content.large .modal-table td:nth-child(7) {
  width: 170px;
}

.modal-content.large .modal-table th:nth-child(8),
.modal-content.large .modal-table td:nth-child(8) {
  width: 70px;
}

.product-qty-cell {
  padding-right: 10px;
}

.product-check-head,
.product-check-cell {
  width: 54px;
  text-align: center;
}

.product-check-cell {
  padding-left: 10px;
}

.product-check-cell .product-check {
  margin: 0 auto;
}

.small-table {
  table-layout: fixed;
  font-size: 12px;
}

.small-table th,
.small-table td {
  padding: 8px 6px;
  font-size: 12px;
}

/* select button */
.btn-select {
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  color: #fff;
  border: none;
  padding: 8px 10px;
  border-radius: 12px;
  font-weight: 900;
  cursor: pointer;
}

/* ===== PAYMENT MODAL ===== */
.payment-modal {
  width: 440px;
  max-width: 95vw;
  padding: 16px;
  animation: pop .18s ease;
}

@keyframes pop {
  from {
    transform: translateY(6px) scale(.98);
    opacity: 0;
  }

  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.payment-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.payment-tabs .tab-item {
  flex: 1;
  text-align: center;
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.qr-code img {
  width: 220px;
  height: 220px;
  object-fit: contain;
}

.payment-footer {
  margin-top: 14px;
}

.order-type-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-weight: 600;
}

.order-type-toggle span {
  color: #9ca3af;
}

.order-type-toggle span.active {
  color: #2563eb;
}

/* SWITCH */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background: #d1d5db;
  border-radius: 20px;
  transition: 0.3s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.3s;
}

.switch input:checked+.slider {
  background: #2563eb;
}

.switch input:checked+.slider:before {
  transform: translateX(24px);
}

/* ===== VOUCHER SUGGESTION BANNER ===== */
.voucher-suggestion {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  margin-top: 10px;
  border-radius: 14px;
  border: 1px solid #fde68a;
  background: linear-gradient(180deg, #fffbeb, #fef3c7);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.1);
}

.suggestion-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 1px;
}

.suggestion-text {
  font-size: 13px;
  color: #92400e;
  line-height: 1.6;
}

.suggestion-detail {
  color: #b45309;
}

.highlight-save {
  color: #dc2626;
  font-weight: 900;
}

/* ===== NEW VOUCHER NOTIFY MODAL ===== */
.voucher-notify-modal {
  width: 480px;
  max-width: 95vw;
  animation: pop .18s ease;
}

.voucher-notify-body {
  padding: 8px 0 16px;
}

.voucher-notify-body p {
  margin: 0 0 14px;
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
}

.voucher-compare {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  margin-bottom: 14px;
}

.voucher-old,
.voucher-new {
  flex: 1;
  text-align: center;
}

.compare-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 700;
  margin-bottom: 6px;
}

.compare-value {
  font-size: 20px;
  font-weight: 900;
  color: #475569;
}

.compare-value.highlight {
  color: #16a34a;
}

.compare-name {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.compare-arrow {
  font-size: 22px;
  color: #94a3b8;
  font-weight: 900;
}

.compare-benefit {
  text-align: center;
  font-size: 14px;
  color: #16a34a;
}

/* Voucher chưa đủ điều kiện áp dụng */
.voucher-not-applicable {
  opacity: 0.55;
  background: #fef2f2;
}

.voucher-not-applicable td {
  color: #94a3b8 !important;
}

/* ===== BRAND THEME (LOGO) ===== */
.pos-page {
  --brand-navy: #223f67;
  --brand-navy-strong: #1b3252;
  --brand-cream: #ece3d2;
  --brand-bg: #edf1f6;
  --brand-line: #d5ddea;
  --brand-text: #1f2a3b;
  --brand-sub: #607089;
  --brand-danger: #c53131;
  --brand-success: #168a55;
  padding: 6px;
  background: radial-gradient(circle at top right, #f7f9fc 0%, var(--brand-bg) 60%);
  color: var(--brand-text);
  font-family: "Be Vietnam Pro", "Segoe UI", sans-serif;
}

.pos-sub,
.muted,
.pay-note,
.name-sub {
  color: var(--brand-sub);
}

.pos-badge {
  color: var(--brand-navy);
  background: linear-gradient(180deg, #f8f5ed, #ffffff);
  border-color: #e5dece;
}

.btn-primary,
.btn-select {
  background: linear-gradient(180deg, var(--brand-navy), var(--brand-navy-strong));
  box-shadow: 0 10px 18px rgba(34, 63, 103, 0.24);
}

.btn-outline {
  border-color: #c6d2e4;
  color: var(--brand-navy);
}

.btn-outline:hover {
  background: #f4f8ff;
}

.order-tab.active {
  background: linear-gradient(180deg, var(--brand-navy), var(--brand-navy-strong));
  border-color: rgba(34, 63, 103, 0.45);
}

.p-price,
.price-col,
.price-col.total,
.highlight-save {
  color: var(--brand-danger);
}

.btn-pay {
  background: linear-gradient(180deg, #1f9659, var(--brand-success));
}

.card,
.pos-topbar,
.tab-counter,
.order-tab {
  border-color: var(--brand-line);
}

.modal-content,
.payment-modal {
  border-color: var(--brand-line);
}

/* Căn cột rõ ràng để modal khách hàng không bị lệch nút/chữ */
.customer-table {
  table-layout: fixed;
  width: 100%;
}

.customer-modal {
  width: 680px;
  max-width: 95vw;
}

.customer-table thead th,
.customer-table tbody td {
  text-align: left;
}

.customer-table th:nth-child(1),
.customer-table td:nth-child(1) {
  width: 30%;
}

.customer-table th:nth-child(2),
.customer-table td:nth-child(2) {
  width: 18%;
}

.customer-table th:nth-child(3),
.customer-table td:nth-child(3) {
  width: 34%;
}

.customer-table th:nth-child(4),
.customer-table td:nth-child(4) {
  width: 18%;
  text-align: center;
}

/* Override rule chung .modal-table *:first-child để cột tên KH không bị bó còn 120px */
.customer-table.modal-table td:first-child,
.customer-table.modal-table th:first-child {
  width: 30%;
}

.customer-table .name-cell {
  min-width: 0;
}

.customer-table .name-main {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-table .email-cell {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.customer-table .action-cell {
  text-align: center;
}

.customer-table .phone-cell {
  white-space: nowrap;
}

.customer-table .btn-select {
  min-width: 72px;
}
</style>