from flask import Flask, render_template, request, redirect, url_for, flash, send_file, jsonify
import json
import os
from datetime import datetime
import secrets

app = Flask(__name__)
app.secret_key = 'your-secret-key-here'

# مسیرهای فایل‌ها
USERS_FILE = 'data/users.json'
SIGNALS_FILE = 'data/signals.json'
SITES_FILE = 'data/sites.json'
SUBMISSIONS_FILE = 'data/submissions.json'

# ایجاد پوشه data در صورت عدم وجود
os.makedirs('data', exist_ok=True)
os.makedirs('static/uploads', exist_ok=True)

def load_json_file(filepath, default=None):
    """بارگذاری فایل JSON"""
    if default is None:
        default = []
    try:
        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as f:
                return json.load(f)
        return default
    except:
        return default

def save_json_file(filepath, data):
    """ذخیره فایل JSON"""
    try:
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        return True
    except:
        return False

@app.route('/')
def index():
    """صفحه اصلی"""
    return render_template('index.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    """ثبت نام کاربران"""
    if request.method == 'POST':
        username = request.form.get('username', '').strip()
        password = request.form.get('password', '').strip()
        
        if not username or not password:
            return jsonify({'success': False, 'message': 'نام کاربری و رمز عبور الزامی است'})
        
        # بارگذاری کاربران موجود
        users = load_json_file(USERS_FILE, [])
        
        # بررسی تکراری نبودن نام کاربری
        for user in users:
            if user.get('username') == username:
                return jsonify({'success': False, 'message': 'نام کاربری قبلاً ثبت شده است'})
        
        # ایجاد کد تأیید 5 رقمی
        verification_code = str(secrets.randbelow(100000)).zfill(5)
        
        # اضافه کردن کاربر جدید
        new_user = {
            'username': username,
            'password': password,
            'verification_code': verification_code,
            'verified': True,  # فرض می‌کنیم تأیید شده
            'registered_at': datetime.now().isoformat()
        }
        
        users.append(new_user)
        
        # ذخیره در فایل
        if save_json_file(USERS_FILE, users):
            return jsonify({'success': True, 'message': 'ثبت نام با موفقیت انجام شد'})
        else:
            return jsonify({'success': False, 'message': 'خطا در ثبت نام'})
    
    return render_template('abt.html')

@app.route('/signals')
def signals():
    """صفحه سیگنال‌ها"""
    signals = load_json_file(SIGNALS_FILE, [])
    return render_template('sig.html', signals=signals)

@app.route('/sites')
def sites():
    """صفحه سایت‌ها"""
    sites = load_json_file(SITES_FILE, [])
    return render_template('sait.html', sites=sites)

@app.route('/submit-site', methods=['GET', 'POST'])
def submit_site():
    """ثبت سایت جدید"""
    if request.method == 'POST':
        site_name = request.form.get('site_name', '').strip()
        site_url = request.form.get('site_url', '').strip()
        site_description = request.form.get('site_description', '').strip()
        
        if not all([site_name, site_url, site_description]):
            return jsonify({'success': False, 'message': 'تمام فیلدها الزامی است'})
        
        # بارگذاری سایت‌های موجود
        sites = load_json_file(SITES_FILE, [])
        
        # اضافه کردن سایت جدید
        new_site = {
            'id': len(sites) + 1,
            'name': site_name,
            'url': site_url,
            'description': site_description,
            'likes': 0,
            'submitted_at': datetime.now().isoformat()
        }
        
        sites.append(new_site)
        
        # ذخیره در فایل سایت‌ها
        save_json_file(SITES_FILE, sites)
        
        # ذخیره در فایل ارسال‌ها برای مدیریت
        submissions = load_json_file(SUBMISSIONS_FILE, [])
        submissions.append(new_site)
        save_json_file(SUBMISSIONS_FILE, submissions)
        
        return jsonify({'success': True, 'message': 'سایت با موفقیت ثبت شد'})
    
    return render_template('submit_site.html')

@app.route('/like-site/<int:site_id>')
def like_site(site_id):
    """لایک کردن سایت"""
    sites = load_json_file(SITES_FILE, [])
    
    for site in sites:
        if site.get('id') == site_id:
            site['likes'] = site.get('likes', 0) + 1
            break
    
    save_json_file(SITES_FILE, sites)
    return jsonify({'success': True, 'likes': site.get('likes', 0)})

@app.route('/download-python')
def download_python():
    """دانلود فایل پایتون"""
    try:
        return send_file('moai.py', as_attachment=True, download_name='moai.py')
    except:
        return "فایل یافت نشد", 404

@app.route('/download-html')
def download_html():
    """دانلود فایل HTML"""
    try:
        return send_file('git.html', as_attachment=True, download_name='git.html')
    except:
        return "فایل یافت نشد", 404

# مسیرهای مخفی برای مدیریت
@app.route('/admin/broadcast')
def admin_broadcast():
    """ارسال پیام همگانی - مسیر مخفی"""
    return render_template('admin_broadcast.html')

@app.route('/admin/ads')
def admin_ads():
    """مدیریت تبلیغات - مسیر مخفی"""
    return render_template('admin_ads.html')

@app.route('/admin/delete-user', methods=['POST'])
def delete_user():
    """حذف کاربر"""
    username = request.form.get('username', '').strip()
    
    users = load_json_file(USERS_FILE, [])
    users = [user for user in users if user.get('username') != username]
    
    if save_json_file(USERS_FILE, users):
        return jsonify({'success': True, 'message': 'کاربر حذف شد'})
    return jsonify({'success': False, 'message': 'خطا در حذف کاربر'})

if __name__ == '__main__':
    app.run(debug=True)
