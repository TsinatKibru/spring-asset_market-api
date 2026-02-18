"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useApi } from "@/components/hooks/useApi";
import { useTelegram } from "@/components/providers/TelegramProvider";
import CategoryForm from "@/components/CategoryForm";

export default function EditCategoryPage() {
    const { id } = useParams();
    const router = useRouter();
    const { fetchWithAuth } = useApi();
    const { webApp } = useTelegram();
    const [initialData, setInitialData] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        webApp.BackButton.show();
        webApp.BackButton.onClick(() => router.back());

        const loadCategory = async () => {
            try {
                const data = await fetchWithAuth(`/categories/${id}`);
                setInitialData(data);
            } catch (error) {
                console.error("Failed to load category:", error);
                webApp.showAlert("Failed to load category details.");
                router.push("/merchant/categories");
            } finally {
                setLoading(false);
            }
        };

        loadCategory();

        return () => { webApp.BackButton.hide(); };
    }, [id]);

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-black">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
            </div>
        );
    }

    return <CategoryForm initialData={initialData} id={id as string} />;
}
