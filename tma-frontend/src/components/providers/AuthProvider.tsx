"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { useTelegram } from "./TelegramProvider";

interface AuthContextType {
    token: string | null;
    user: any | null;
    isAuthenticated: boolean;
    isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const { webApp, isReady, tenantId } = useTelegram();
    const [token, setToken] = useState<string | null>(null);
    const [user, setUser] = useState<any | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!isReady || !webApp) return;

        const authenticate = async () => {
            try {
                // Determine API base URL
                const apiBase = process.env.NEXT_PUBLIC_API_URL || "/api/v1";

                const response = await fetch(`${apiBase}/telegram/auth`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-Tenant-ID": tenantId || "default",
                        "ngrok-skip-browser-warning": "true",
                    },
                    body: JSON.stringify({
                        initData: webApp.initData,
                    }),
                });

                if (response.ok) {
                    const data = await response.json();
                    setToken(data.accessToken);
                    localStorage.setItem("token", data.accessToken);

                    // Fetch full profile including roles
                    const profileRes = await fetch(`${apiBase}/auth/me`, {
                        headers: {
                            "Authorization": `Bearer ${data.accessToken}`,
                            "X-Tenant-ID": tenantId || "default",
                            "ngrok-skip-browser-warning": "true",
                        }
                    });
                    if (profileRes.ok) {
                        const profile = await profileRes.json();
                        setUser(profile);
                    }
                } else {
                    console.error("Authentication failed");
                }
            } catch (error) {
                console.error("Auth error:", error);
            } finally {
                setIsLoading(false);
            }
        };

        authenticate();
    }, [isReady, webApp?.initData, tenantId]);

    return (
        <AuthContext.Provider value={{ token, user, isAuthenticated: !!token, isLoading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
};
