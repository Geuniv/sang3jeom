// src/hooks/useLogout.js
import { useNavigate } from 'react-router-dom';
import { useCallback } from 'react';
import { createApiInstance } from './axiosInstance';

// 환경변수에서 API URL 가져오기
const USER_SERVICE_URL = import.meta.env.VITE_USER_SERVICE_URL || 'http://localhost:8080';
const authApi = createApiInstance(USER_SERVICE_URL);

export function useLogout(setIsLoggedIn) {
  const navigate = useNavigate();

  return useCallback(async () => {
    console.log('🔒 start logout api');
    await authApi.post('/logout', {});
    console.log('✅ logout API success');
    localStorage.clear();
    sessionStorage.clear();
    setIsLoggedIn(false);
    navigate('/login', { replace: true });
  }, [navigate, setIsLoggedIn]);
}
