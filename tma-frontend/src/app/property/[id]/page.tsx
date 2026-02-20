"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import { useParams, useRouter } from "next/navigation";
import { useApi } from "@/components/hooks/useApi";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { Star, Heart, MessageSquare, Calendar, Package, Bed, Bath, Maximize, Droplets, Warehouse, Home } from "lucide-react";
import ImageCarousel from "@/components/ImageCarousel";

/**
 * Maps attribute names to Lucide icons for a premium look
 */
const attrIconMap: Record<string, any> = {
    bedrooms: Bed,
    bathrooms: Bath,
    sqft: Maximize,
    hasPool: Droplets,
    hasGarage: Warehouse,
    loadingDocks: Package,
    zoningCode: Home,
    floor: Maximize,
};

export default function PropertyDetailPage() {
    const { id } = useParams();
    const router = useRouter();
    const { fetchWithAuth } = useApi();
    const { webApp } = useTelegram();
    const [property, setProperty] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    const [isSaving, setIsSaving] = useState(false);
    const [isSaved, setIsSaved] = useState(false);

    useEffect(() => {
        // Show Back Button in Telegram
        webApp.BackButton.show();
        webApp.BackButton.onClick(() => router.back());

        const loadProperty = async () => {
            try {
                const [data, favorites] = await Promise.all([
                    fetchWithAuth(`/properties/${id}`),
                    fetchWithAuth("/favorites")
                ]);

                setProperty(data);

                // Extract list from Page object
                const savedList = favorites?.content || favorites || [];
                const found = savedList.find((f: any) => (f.id === Number(id) || f.property?.id === Number(id)));
                setIsSaved(!!found);
            } catch (error) {
                console.error("Failed to load property:", error);
            } finally {
                setLoading(false);
            }
        };

        loadProperty();

        return () => {
            webApp.BackButton.hide();
        };
    }, [id]);

    const handleRequestViewing = async () => {
        webApp.HapticFeedback.impactOccurred('medium');
        setIsSaving(true);
        try {
            await fetchWithAuth("/viewings/request", {
                method: "POST",
                body: JSON.stringify({
                    propertyId: id,
                    requestedAt: new Date().toISOString().slice(0, 19),
                    notes: "Requested via Telegram Mini App"
                })
            });
            webApp.showAlert("Viewing Requested! You will receive a notification when confirmed.");
        } catch (error) {
            console.error("Viewing request failed:", error);
            webApp.showAlert("Failed to send request.");
        } finally {
            setIsSaving(false);
        }
    };

    const handleSendInquiry = async () => {
        webApp.HapticFeedback.impactOccurred('light');
        const message = await new Promise((resolve) => {
            webApp.showPopup({
                title: "Send Inquiry",
                message: "Ask anything about this property:",
                buttons: [{ id: "send", type: "default", text: "Send" }, { id: "cancel", type: "cancel" }]
            }, (btnId: string | undefined) => resolve(btnId === "send"));
        });

        if (!message) return;

        try {
            await fetchWithAuth("/messages/inquiry", {
                method: "POST",
                body: JSON.stringify({
                    propertyId: id,
                    content: "Inquiry from Telegram Mini App"
                })
            });
            webApp.showAlert("Message Sent!");
        } catch (error) {
            webApp.showAlert("Failed to send message.");
        }
    };

    const toggleFavorite = async () => {
        if (isSaving) return;

        webApp.HapticFeedback.selectionChanged();
        const prevStatus = isSaved;
        setIsSaved(!prevStatus);

        try {
            await fetchWithAuth(`/favorites/${id}`, {
                method: "POST"
            });
            webApp.showConfirm(prevStatus ? "Removed from Saved" : "Added to Saved");
        } catch (error) {
            console.error("Failed to toggle favorite:", error);
            setIsSaved(prevStatus); // Rollback
            webApp.showAlert("Failed to update favorite status.");
        }
    };

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-white dark:bg-black">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
            </div>
        );
    }

    if (!property) {
        return (
            <div className="flex min-h-screen flex-col items-center justify-center bg-white px-6 text-center dark:bg-black">
                <span className="text-4xl">🏷️</span>
                <h2 className="mt-4 text-xl font-bold dark:text-white">Property Not Found</h2>
                <button
                    onClick={() => router.push("/")}
                    className="mt-6 rounded-full bg-zinc-900 px-6 py-2 text-sm font-bold text-white dark:bg-white dark:text-black"
                >
                    Back to Marketplace
                </button>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-white pb-32 dark:bg-black">
            {/* Image Gallery */}
            <div className="relative aspect-[4/3] w-full overflow-hidden bg-zinc-100 dark:bg-zinc-900">
                <ImageCarousel
                    images={property.imageUrls || []}
                    title={property.title}
                />
            </div>

            <div className="px-6 pt-8">
                <div className="flex items-center justify-between">
                    <span className="rounded-full bg-zinc-100 px-3 py-1 text-[10px] font-black uppercase tracking-widest text-zinc-500 dark:bg-zinc-800 dark:text-zinc-400">
                        {property.categoryName}
                    </span>
                    <div className="flex items-center gap-1 text-sm font-bold text-amber-500">
                        <Star size={14} fill="currentColor" />
                        <span>{property.averageRating?.toFixed(1) || "New"}</span>
                    </div>
                </div>

                <h1 className="mt-4 text-3xl font-black leading-tight text-zinc-900 dark:text-white">
                    {property.title}
                </h1>

                <p className="mt-2 text-lg font-medium text-zinc-500 dark:text-zinc-400">
                    {property.location}
                </p>

                <div className="mt-8 flex items-center justify-between border-y border-zinc-100 py-6 dark:border-zinc-800">
                    <div>
                        <p className="text-xs font-bold uppercase tracking-widest text-zinc-400">Price</p>
                        <p className="text-2xl font-black text-zinc-900 dark:text-white">
                            ${property.price.toLocaleString()}
                        </p>
                    </div>
                    <button
                        onClick={toggleFavorite}
                        className={`rounded-full p-3 transition-all active:scale-90 ${isSaved ? "bg-red-50 text-red-500 dark:bg-red-900/20" : "bg-zinc-100 text-zinc-400 dark:bg-zinc-800"
                            }`}
                    >
                        <Heart size={20} fill={isSaved ? "currentColor" : "none"} />
                    </button>
                </div>

                <div className="mt-8">
                    <h3 className="text-xs font-bold uppercase tracking-widest text-zinc-400">Description</h3>
                    <p className="mt-4 text-lg leading-relaxed text-zinc-600 dark:text-zinc-400">
                        {property.description || "Discover the pinnacle of luxury living in this exquisite property. Meticulously designed with premium finishes and unparalleled attention to detail."}
                    </p>
                </div>

                {/* Dynamic Specifications */}
                {property.attributes && Object.keys(property.attributes).length > 0 && (
                    <div className="mt-10">
                        <h3 className="text-xs font-bold uppercase tracking-widest text-zinc-400">Specifications</h3>
                        <div className="mt-6 grid grid-cols-2 gap-4">
                            {Object.entries(property.attributes as Record<string, any>).map(([key, value]) => {
                                const Icon = attrIconMap[key] || Package;
                                const displayValue = typeof value === 'boolean'
                                    ? (value ? 'Yes' : 'No')
                                    : String(value);

                                return (
                                    <div key={key} className="flex items-center gap-4 rounded-2xl bg-zinc-50 p-4 dark:bg-zinc-800/50">
                                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white text-zinc-900 shadow-sm dark:bg-zinc-800 dark:text-white">
                                            <Icon size={18} strokeWidth={2.5} />
                                        </div>
                                        <div>
                                            <p className="text-[10px] font-black uppercase tracking-wider text-zinc-400">{key}</p>
                                            <p className="text-sm font-black text-zinc-900 dark:text-white">{displayValue}</p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}

                {/* Call to Action */}
                <div className="fixed bottom-24 left-0 right-0 px-6 backdrop-blur-md bg-white/50 dark:bg-black/50 py-4">
                    <div className="flex gap-4">
                        <button
                            onClick={handleSendInquiry}
                            className="flex-1 rounded-2xl bg-zinc-100 py-5 text-lg font-black text-zinc-900 border border-zinc-200 transition-transform active:scale-[0.98] dark:bg-zinc-800 dark:text-white dark:border-zinc-700"
                        >
                            Message
                        </button>
                        <button
                            disabled={isSaving}
                            onClick={handleRequestViewing}
                            className={`flex-[2] rounded-2xl py-5 text-lg font-black transition-transform active:scale-[0.98] shadow-xl ${isSaving
                                ? "bg-zinc-400 cursor-not-allowed"
                                : "bg-zinc-900 text-white dark:bg-white dark:text-black"
                                }`}
                        >
                            {isSaving ? "Sending..." : "Request Viewing"}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
