<template>
  <div class="page-container">
    
    <div class="page-header">
       <h1>LIÊN HỆ VỚI CHÚNG TÔI</h1>
       <p>Hãy để lại lời nhắn, chúng tôi sẽ phản hồi bạn trong thời gian sớm nhất.</p>
    </div>

    <div class="contact-layout">
       
       <div class="contact-form">
          <form @submit.prevent="sendMessage">
             <div class="form-group">
                <label>Họ và tên</label>
                <input 
                    type="text" 
                    v-model="form.name" 
                    placeholder="Nhập họ tên của bạn" 
                    class="c-input" 
                    required
                >
             </div>
             <div class="form-group">
                <label>Email</label>
                <input 
                    type="email" 
                    v-model="form.email" 
                    placeholder="Nhập địa chỉ email" 
                    class="c-input" 
                    required
                >
             </div>
             <div class="form-group">
                <label>Nội dung</label>
                <textarea 
                    rows="5" 
                    v-model="form.message" 
                    placeholder="Nội dung lời nhắn..." 
                    class="c-input" 
                    required
                ></textarea>
             </div>
             <button type="submit" class="btn-send">
                <i class="fas fa-paper-plane"></i> GỬI TIN NHẮN
             </button>
          </form>
       </div>

       <div class="contact-info">
          <div class="info-box">
             <h3>YOURCHOICE SHOP</h3>
             <div class="info-item">
                <div class="icon-wrap"><i class="fas fa-map-marker-alt"></i></div>
                <p>Số 1, Trịnh Văn Bô, Nam Từ Liêm, Hà Nội</p>
             </div>
             <div class="info-item">
                <div class="icon-wrap"><i class="fas fa-phone-alt"></i></div>
                <p>0912.345.678</p>
             </div>
             <div class="info-item">
                <div class="icon-wrap"><i class="fas fa-envelope"></i></div>
                <p>support@yourchoice.vn</p>
             </div>
             <div class="info-item">
                <div class="icon-wrap"><i class="fas fa-clock"></i></div>
                <p>8:00 - 22:00 (Hàng ngày)</p>
             </div>
          </div>

          <div class="map-box">
             <iframe 
                src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3723.863981044336!2d105.74459841533215!3d21.03812779283522!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x313454b991d80fd5%3A0x53cefc99d6b0bf6f!2zVHLGsOG7nW5nIENhbyDEkTXchW5nIEZQVCBQb2x5dGVjaG5pYw!5e0!3m2!1svi!2s!4v1647850062483!5m2!1svi!2s" 
                width="100%" 
                height="100%" 
                style="border:0;" 
                allowfullscreen="" 
                loading="lazy">
             </iframe>
          </div>
       </div>

    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import Swal from 'sweetalert2';

const form = reactive({
    name: '',
    email: '',
    message: ''
});

const sendMessage = () => {
    // Giả lập gửi tin nhắn thành công
    Swal.fire({
        icon: 'success',
        title: 'Gửi thành công!',
        text: 'Cảm ơn bạn đã liên hệ. Chúng tôi sẽ phản hồi sớm nhất.',
        confirmButtonColor: '#0f172a'
    });
    
    // Reset form
    form.name = '';
    form.email = '';
    form.message = '';
};
</script>

<style scoped>
/* GLOBAL */
.page-container { 
    max-width: 1280px; margin: 0 auto; padding: 60px 20px; 
    font-family: Arial, sans-serif; color: #333; 
}

/* HEADER */
.page-header { text-align: center; margin-bottom: 50px; }
.page-header h1 { font-size: 32px; color: #0f172a; margin-bottom: 10px; font-weight: 800; }
.page-header p { color: #64748b; font-size: 16px; }

/* LAYOUT */
.contact-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 60px; }

/* FORM */
.contact-form { background: #fff; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 14px; color: #0f172a; }

.c-input { 
    width: 100%; padding: 12px 15px; 
    border: 1px solid #cbd5e1; border-radius: 6px; 
    font-family: Arial, sans-serif; font-size: 14px; outline: none;
    transition: 0.2s; box-sizing: border-box;
}
.c-input:focus { border-color: #1e3a8a; box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1); }

.btn-send { 
    width: 100%; padding: 14px; 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: white; border: none; border-radius: 6px; 
    font-weight: 700; font-size: 15px; cursor: pointer; 
    transition: 0.3s; display: flex; justify-content: center; align-items: center; gap: 10px;
}
.btn-send:hover { opacity: 0.9; box-shadow: 0 5px 15px rgba(15, 23, 42, 0.2); transform: translateY(-1px); }

/* INFO BOX */
.info-box { 
    background: #f8fafc; padding: 30px; border-radius: 12px; margin-bottom: 30px; 
    border: 1px solid #e2e8f0;
}
.info-box h3 { margin-top: 0; margin-bottom: 20px; color: #1e3a8a; font-size: 20px; font-weight: 800; letter-spacing: 1px; }

.info-item { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }
.icon-wrap { 
    width: 40px; height: 40px; background: white; border-radius: 50%; 
    display: flex; align-items: center; justify-content: center; 
    color: #1e3a8a; box-shadow: 0 2px 5px rgba(0,0,0,0.05); font-size: 18px;
}
.info-item p { margin: 0; color: #334155; font-size: 15px; }

/* MAP BOX */
.map-box { 
    width: 100%; height: 280px; border-radius: 12px; overflow: hidden; 
    box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #e2e8f0;
}

/* RESPONSIVE */
@media (max-width: 768px) {
    .contact-layout { grid-template-columns: 1fr; gap: 40px; }
    .page-header h1 { font-size: 26px; }
}
</style>