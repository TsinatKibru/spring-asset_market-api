"use client";

import { createContext, useContext, useEffect, useState } from "react";

interface TelegramContextType {
    user: any;
    webApp: any;
    tenantId: string | null;
    isReady: boolean;
}

const TelegramContext = createContext<TelegramContextType | null>(null);

export const TelegramProvider = ({ children }: { children: React.ReactNode }) => {
    const [isReady, setIsReady] = useState(false);
    const [user, setUser] = useState<any>(null);
    const [tenantId, setTenantId] = useState<string | null>(null);
    const [webApp, setWebApp] = useState<any>(null);

    useEffect(() => {
        const initTG = async () => {
            if (typeof window !== "undefined") {
                try {
                    // Dynamically import to avoid SSR issues
                    const WebApp = (await import("@twa-dev/sdk")).default;

                    WebApp.ready();
                    WebApp.expand();

                    setWebApp(WebApp);
                    setUser(WebApp.initDataUnsafe?.user || null);

                    const urlParams = new URLSearchParams(window.location.search);
                    const urlTenant = urlParams.get("tenant");
                    const startParam = WebApp.initDataUnsafe?.start_param || urlTenant || "default";

                    setTenantId(startParam);

                    // Set theme colors
                    const root = document.documentElement;
                    const { themeParams } = WebApp;

                    if (themeParams.bg_color) root.style.setProperty("--tg-theme-bg-color", themeParams.bg_color);
                    if (themeParams.text_color) root.style.setProperty("--tg-theme-text-color", themeParams.text_color);
                    if (themeParams.hint_color) root.style.setProperty("--tg-theme-hint-color", themeParams.hint_color);
                    if (themeParams.link_color) root.style.setProperty("--tg-theme-link-color", themeParams.link_color);
                    if (themeParams.button_color) root.style.setProperty("--tg-theme-button-color", themeParams.button_color);
                    if (themeParams.button_text_color) root.style.setProperty("--tg-theme-button-text-color", themeParams.button_text_color);

                    setIsReady(true);
                } catch (err) {
                    console.error("Telegram WebApp failed to load:", err);
                    // Fallback for development if not in Telegram
                    setIsReady(true);
                    setTenantId("default");
                }
            }
        };

        initTG();
    }, []);

    return (
        <TelegramContext.Provider value={{ user, webApp, tenantId, isReady }}>
            {children}
        </TelegramContext.Provider>
    );
};

export const useTelegram = () => {
    const context = useContext(TelegramContext);
    if (!context) {
        throw new Error("useTelegram must be used within a TelegramProvider");
    }
    return context;
};
