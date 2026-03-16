import axios from 'axios';

/**
 * API địa chỉ hành chính Việt Nam sau sáp nhập (esgoo.net)
 * Trả về dữ liệu tương thích format cũ: { code, name }
 */

const BASE_URL = 'https://esgoo.net/api-tinhthanh';

const mapItem = (item) => ({
  code: item.id,
  name: item.full_name,
});

export const fetchProvinces = async () => {
  const res = await axios.get(`${BASE_URL}/1/0.htm`);
  if (res.data.error !== 0) throw new Error('Lỗi tải tỉnh/thành');
  return res.data.data.map(mapItem);
};

export const fetchDistricts = async (provinceCode) => {
  const res = await axios.get(`${BASE_URL}/2/${provinceCode}.htm`);
  if (res.data.error !== 0) throw new Error('Lỗi tải quận/huyện');
  return res.data.data.map(mapItem);
};

export const fetchWards = async (districtCode) => {
  const res = await axios.get(`${BASE_URL}/3/${districtCode}.htm`);
  if (res.data.error !== 0) throw new Error('Lỗi tải phường/xã');
  return res.data.data.map(mapItem);
};
