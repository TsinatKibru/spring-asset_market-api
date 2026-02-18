"use client";

import CategoryForm from "@/components/CategoryForm";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function AddCategoryPage() {
    const { webApp } = useTelegram();
    const router = useRouter();

    useEffect(() => {
        webApp.BackButton.show();
        webApp.BackButton.onClick(() => router.back());
        return () => { webApp.BackButton.hide(); };
    }, []);

    return <CategoryForm />;
}
