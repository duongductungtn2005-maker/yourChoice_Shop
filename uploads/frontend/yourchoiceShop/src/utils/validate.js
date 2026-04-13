// File: src/utils/validate.js

export const validate = {
  // 1. Kiểm tra rỗng
  isRequired: (value) => {
    if (value === null || value === undefined || String(value).trim() === '') {
      return 'Trường này không được để trống';
    }
    return null;
  },

  // 2. Kiểm tra Email
  isEmail: (value) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(value) ? null : 'Email không đúng định dạng';
  },

  // 3. Kiểm tra Số điện thoại (VN)
  isPhone: (value) => {
    const regex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
    return regex.test(value) ? null : 'Số điện thoại không hợp lệ (10 số, đầu 03,05,07,08,09)';
  },

  // 4. Kiểm tra số nguyên dương (Số lượng)
  isPositiveInteger: (value) => {
    if (!Number.isInteger(Number(value)) || Number(value) <= 0) {
      return 'Vui lòng nhập số nguyên dương lớn hơn 0';
    }
    return null;
  },

  // 5. Kiểm tra số thực dương (Tiền)
  isPositiveNumber: (value) => {
    if (isNaN(value) || Number(value) < 0) {
      return 'Giá trị không được âm';
    }
    return null;
  },

  // 6. Kiểm tra ngày kết thúc > ngày bắt đầu
  isAfterDate: (startDate, endDate) => {
    if (!startDate || !endDate) return null;
    return new Date(endDate) > new Date(startDate) ? null : 'Ngày kết thúc phải sau ngày bắt đầu';
  },

  // 7. Kiểm tra độ dài tối thiểu
  minLength: (value, min) => {
    return String(value).length >= min ? null : `Phải nhập tối thiểu ${min} ký tự`;
  }
};