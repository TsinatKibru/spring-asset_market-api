"use client";

import { useAuth } from "../providers/AuthProvider";
import { useTelegram } from "../providers/TelegramProvider";

export const useApi = () => {
    const { token } = useAuth();
    const { tenantId } = useTelegram();
    const baseUrl = process.env.NEXT_PUBLIC_API_URL;

    const fetchWithAuth = async (endpoint: string, options: RequestInit = {}) => {
        const headers = {
            ...options.headers,
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            "X-Tenant-ID": tenantId || "default",
            "ngrok-skip-browser-warning": "true",
        };

        const response = await fetch(`${baseUrl}${endpoint}`, {
            ...options,
            headers,
        });

        if (!response.ok) {
            let errorMsg = `API Error: ${response.statusText}`;
            try {
                const errorData = await response.json();
                errorMsg = errorData.message || errorData.error || errorMsg;
            } catch (e) {
                // Not JSON or no message
            }
            throw new Error(errorMsg);
        }

        return response.json();
    };

    const postWithAuth = async (endpoint: string, body: any) => {
        return fetchWithAuth(endpoint, {
            method: "POST",
            body: JSON.stringify(body),
        });
    };

    return { fetchWithAuth, postWithAuth };
};
