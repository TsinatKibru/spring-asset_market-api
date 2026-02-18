"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/components/providers/AuthProvider";
import { useApi } from "@/components/hooks/useApi";
import PropertyCard from "@/components/PropertyCard";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { Search, LayoutGrid } from "lucide-react";

export default function Home() {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const { fetchWithAuth } = useApi();
  const { webApp } = useTelegram();
  const [properties, setProperties] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [categoriesLoading, setCategoriesLoading] = useState(true);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const data = await fetchWithAuth("/categories");
        setCategories(data || []);
      } catch (error) {
        console.error("Failed to load categories:", error);
      } finally {
        setCategoriesLoading(false);
      }
    };
    loadCategories();
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      const loadProperties = async () => {
        setLoading(true);
        try {
          const url = selectedCategory
            ? `/properties?category=${encodeURIComponent(selectedCategory)}`
            : "/properties";
          const data = await fetchWithAuth(url);
          setProperties(data.content || data || []);
        } catch (error) {
          console.error("Failed to load properties:", error);
        } finally {
          setLoading(false);
        }
      };
      loadProperties();
    }
  }, [isAuthenticated, selectedCategory]);

  const handleCategorySelect = (categoryName: string | null) => {
    webApp.HapticFeedback.impactOccurred('light');
    setSelectedCategory(categoryName);
  };

  if (authLoading || loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-white dark:bg-black">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-zinc-50 px-4 pt-6 pb-24 dark:bg-black">
      <header className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-white">
            Marketplace
          </h1>
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            Discover premium listings
          </p>
        </div>
        <div className="h-10 w-10 overflow-hidden rounded-full bg-zinc-200 ring-2 ring-white dark:bg-zinc-800 dark:ring-zinc-900">
          <div className="flex h-full w-full items-center justify-center text-xs font-bold text-zinc-500">
            TG
          </div>
        </div>
      </header>

      {/* Category Scroller */}
      <div className="mb-8 -mx-4 overflow-x-auto no-scrollbar flex gap-3 px-4 pb-2">
        <button
          onClick={() => handleCategorySelect(null)}
          className={`flex-none flex items-center gap-2 rounded-2xl px-5 py-3 text-sm font-bold transition-all active:scale-95 ${selectedCategory === null
              ? "bg-zinc-900 text-white dark:bg-white dark:text-black shadow-lg"
              : "bg-white text-zinc-500 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:text-zinc-400 dark:ring-zinc-800"
            }`}
        >
          <LayoutGrid size={16} />
          All Assets
        </button>
        {categories.map((cat) => (
          <button
            key={cat.id}
            onClick={() => handleCategorySelect(cat.name)}
            className={`flex-none rounded-2xl px-5 py-3 text-sm font-bold transition-all active:scale-95 ${selectedCategory === cat.name
                ? "bg-zinc-900 text-white dark:bg-white dark:text-black shadow-lg"
                : "bg-white text-zinc-500 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:text-zinc-400 dark:ring-zinc-800"
              }`}
          >
            {cat.name}
          </button>
        ))}
        {categoriesLoading && (
          <div className="flex gap-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-11 w-24 animate-pulse rounded-2xl bg-zinc-200 dark:bg-zinc-800" />
            ))}
          </div>
        )}
      </div>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {properties.map((property) => (
          <PropertyCard key={property.id} property={property} />
        ))}
      </div>

      {properties.length === 0 && (
        <div className="mt-20 text-center">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-zinc-100 dark:bg-zinc-900">
            <Search size={32} className="text-zinc-300 dark:text-zinc-600" />
          </div>
          <p className="mt-6 text-lg font-bold text-zinc-900 dark:text-white">No properties found</p>
          <p className="text-sm text-zinc-500">Check back later for new arrivals</p>
        </div>
      )}
    </div>
  );
}
