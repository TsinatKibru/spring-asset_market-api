"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useApi } from "@/components/hooks/useApi";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { Plus, Edit2, Trash2, ArrowLeft, Layers, Info } from "lucide-react";

export default function CategoryListPage() {
    const router = useRouter();
    const { fetchWithAuth } = useApi();
    const { webApp } = useTelegram();
    const [categories, setCategories] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        webApp.BackButton.show();
        webApp.BackButton.onClick(() => router.push("/merchant/dashboard"));
        loadCategories();
        return () => { webApp.BackButton.hide(); };
    }, []);

    const loadCategories = async () => {
        setLoading(true);
        try {
            const data = await fetchWithAuth("/categories");
            setCategories(data || []);
        } catch (error) {
            console.error("Failed to load categories:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number, name: string) => {
        webApp.showPopup({
            title: "Delete Asset Type?",
            message: `Are you sure you want to remove "${name}"? This will fail if properties are currently using this type.`,
            buttons: [
                { id: "delete", type: "destructive", text: "Delete" },
                { type: "cancel" }
            ]
        }, async (btnId) => {
            if (btnId === "delete") {
                webApp.HapticFeedback.impactOccurred("medium");
                try {
                    await fetchWithAuth(`/categories/${id}`, { method: "DELETE" });
                    webApp.showConfirm("Category deleted successfully.");
                    loadCategories();
                } catch (error: any) {
                    webApp.showAlert(error.message || "Failed to delete category. Ensure no properties are linked.");
                }
            }
        });
    };

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-black">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-zinc-50 pb-24 dark:bg-black">
            <header className="bg-white px-6 pt-8 pb-6 shadow-sm dark:bg-zinc-900">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-black tracking-tight text-zinc-900 dark:text-white">
                            Asset Types
                        </h1>
                        <p className="text-xs font-bold uppercase tracking-widest text-zinc-400">
                            Category Architect
                        </p>
                    </div>
                    <button
                        onClick={() => router.push("/merchant/categories/add")}
                        className="rounded-full bg-zinc-900 p-3 text-white shadow-lg active:scale-95 dark:bg-white dark:text-black"
                    >
                        <Plus size={24} />
                    </button>
                </div>
            </header>

            <div className="px-6 pt-8">
                <div className="space-y-4">
                    {categories.length === 0 ? (
                        <div className="mt-20 text-center">
                            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-zinc-100 dark:bg-zinc-900">
                                <Layers size={32} className="text-zinc-300 dark:text-zinc-600" />
                            </div>
                            <p className="mt-6 text-lg font-bold text-zinc-900 dark:text-white">No asset types yet</p>
                            <p className="text-sm text-zinc-500">Create your first category to start listing properties</p>
                        </div>
                    ) : (
                        categories.map((cat) => (
                            <div
                                key={cat.id}
                                className="group relative overflow-hidden rounded-2xl bg-white p-5 ring-1 ring-zinc-100 transition-all hover:shadow-md dark:bg-zinc-900 dark:ring-zinc-800"
                            >
                                <div className="flex items-start justify-between">
                                    <div className="flex-1">
                                        <h3 className="text-lg font-black text-zinc-900 dark:text-white">
                                            {cat.name}
                                        </h3>
                                        <p className="mt-1 line-clamp-2 text-sm text-zinc-500 dark:text-zinc-400">
                                            {cat.description || "No description provided."}
                                        </p>
                                        <div className="mt-4 flex flex-wrap gap-2">
                                            {cat.attributeSchema?.map((attr: any) => (
                                                <span
                                                    key={attr.name}
                                                    className="inline-flex items-center rounded-full bg-zinc-100 px-2.5 py-0.5 text-[10px] font-bold text-zinc-500 dark:bg-zinc-800 dark:text-zinc-400"
                                                >
                                                    {attr.name}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                    <div className="ml-4 flex flex-col gap-2">
                                        <button
                                            onClick={() => router.push(`/merchant/categories/${cat.id}/edit`)}
                                            className="rounded-full bg-zinc-50 p-2 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-900 dark:bg-zinc-800 dark:hover:bg-zinc-700 dark:hover:text-white"
                                        >
                                            <Edit2 size={18} />
                                        </button>
                                        <button
                                            onClick={() => handleDelete(cat.id, cat.name)}
                                            className="rounded-full bg-zinc-50 p-2 text-zinc-400 transition-colors hover:bg-red-50 hover:text-red-500 dark:bg-zinc-800 dark:hover:bg-red-900/20"
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            <div className="mx-6 mt-12 rounded-2xl bg-zinc-900/5 p-6 dark:bg-white/5">
                <div className="flex items-center gap-3">
                    <Info size={20} className="text-zinc-400" />
                    <h4 className="text-sm font-bold text-zinc-900 dark:text-white">Pro Tip</h4>
                </div>
                <p className="mt-2 text-xs leading-relaxed text-zinc-500 dark:text-zinc-400">
                    Categories define what data you collect for each property. Use descriptive names and clear schemas to make your listings stand out!
                </p>
            </div>
        </div>
    );
}
