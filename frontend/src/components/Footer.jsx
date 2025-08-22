export default function Footer() {
  return (
    <footer className="bg-gray-900 text-white py-8 mt-16">
      <div className="max-w-7xl mx-auto px-4 flex flex-col md:flex-row justify-between items-center">
        <div className="mb-4 md:mb-0 flex items-center space-x-2">
          <img
            src="/logo-magic-dog.png"
            alt="상상상점"
            className="w-8 h-8 object-contain"
          />
          <span className="font-bold text-blue-400">상상상점</span> © 2024. All rights reserved.
        </div>
        <div className="flex gap-4 text-gray-400 text-sm">
          <a href="#">회사소개</a>
          <a href="#">고객센터</a>
          <a href="#">이용약관</a>
          <a href="#">개인정보처리방침</a>
        </div>
      </div>
    </footer>
  );
} 