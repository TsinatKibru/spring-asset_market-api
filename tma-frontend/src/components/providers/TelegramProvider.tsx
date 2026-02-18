"use client";

import { createContext, useContext, useEffect, useState } from "react";
import WebApp from "@twa-dev/sdk";

interface TelegramContextType {
    user: any;
    webApp: typeof WebApp;
    tenantId: string | null;
    isReady: boolean;
}

const TelegramContext = createContext<TelegramContextType | null>(null);

export const TelegramProvider = ({ children }: { children: React.ReactNode }) => {
    const [isReady, setIsReady] = useState(false);
    const [user, setUser] = useState<any>(null);
    const [tenantId, setTenantId] = useState<string | null>(null);

    useEffect(() => {
        // Initialize WebApp
        WebApp.ready();
        WebApp.expand();

        setUser(WebApp.initDataUnsafe?.user || null);

        // Extract tenantId from start_param (deep link) or URL query param
        // Link format: https://t.me/your_bot?start=TENANT_ID
        const urlParams = new URLSearchParams(window.location.search);
        const urlTenant = urlParams.get("tenant");
        const startParam = WebApp.initDataUnsafe?.start_param || urlTenant || "default";

        setTenantId(startParam);

        setIsReady(true);

        // Set theme colors based on TG theme
        const root = document.documentElement;
        const { themeParams } = WebApp;

        if (themeParams.bg_color) root.style.setProperty("--tg-theme-bg-color", themeParams.bg_color);
        if (themeParams.text_color) root.style.setProperty("--tg-theme-text-color", themeParams.text_color);
        if (themeParams.hint_color) root.style.setProperty("--tg-theme-hint-color", themeParams.hint_color);
        if (themeParams.link_color) root.style.setProperty("--tg-theme-link-color", themeParams.link_color);
        if (themeParams.button_color) root.style.setProperty("--tg-theme-button-color", themeParams.button_color);
        if (themeParams.button_text_color) root.style.setProperty("--tg-theme-button-text-color", themeParams.button_text_color);
    }, []);

    return (
        <TelegramContext.Provider value={{ user, webApp: WebApp, tenantId, isReady }}>
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
