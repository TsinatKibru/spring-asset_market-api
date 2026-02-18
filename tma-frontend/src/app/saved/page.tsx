"use client";

import { useEffect, useState } from "react";
import { useApi } from "@/components/hooks/useApi";
import PropertyCard from "@/components/PropertyCard";

export default function SavedPropertiesPage() {
    const { fetchWithAuth } = useApi();
    const [properties, setProperties] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadSaved = async () => {
            try {
                // Endpoint from Phase 13: /api/v1/favorites
                const data = await fetchWithAuth("/favorites");
                // Handle Page object or plain array
                setProperties(data?.content || data || []);
            } catch (error) {
                console.error("Failed to load favorites:", error);
            } finally {
                setLoading(false);
            }
        };
        loadSaved();
    }, []);

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-white dark:bg-black">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-zinc-50 px-4 pt-6 pb-24 dark:bg-black">
            <header className="mb-8">
                <h1 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-white">
                    Saved
                </h1>
                <p className="text-sm text-zinc-500 dark:text-zinc-400">
                    Your favorite luxury picks
                </p>
            </header>

            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {properties.map((property) => (
                    <PropertyCard key={property.id} property={property} />
                ))}
            </div>

            {properties.length === 0 && (
                <div className="mt-20 text-center">
                    <span className="text-4xl">🖤</span>
                    <p className="mt-4 text-lg text-zinc-600 dark:text-zinc-400">No saved properties yet</p>
                    <p className="text-sm text-zinc-500">Tap the heart on any property to save it</p>
                </div>
            )}
        </div>
    );
}
