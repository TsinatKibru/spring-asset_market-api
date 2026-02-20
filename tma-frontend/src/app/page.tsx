"use client";

import { useEffect, useState, useCallback } from "react";
import { useAuth } from "@/components/providers/AuthProvider";
import { useApi } from "@/components/hooks/useApi";
import PropertyCard from "@/components/PropertyCard";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { Search, LayoutGrid, SlidersHorizontal, X } from "lucide-react";

interface Filters {
  location: string;
  minPrice: string;
  maxPrice: string;
  status: string;
  attributes: Record<string, string>;
  sortBy: string;
  sortDir: string;
}

const DEFAULT_FILTERS: Filters = {
  location: "",
  minPrice: "",
  maxPrice: "",
  status: "",
  attributes: {},
  sortBy: "createdAt",
  sortDir: "DESC",
};

export default function Home() {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const { fetchWithAuth } = useApi();
  const { webApp, isReady } = useTelegram();

  const [properties, setProperties] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<any | null>(null);
  const [loading, setLoading] = useState(false);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState<Filters>(DEFAULT_FILTERS);
  const [showFilters, setShowFilters] = useState(false);
  const [activeFilterCount, setActiveFilterCount] = useState(0);

  // Load categories
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
    if (isReady) {
      loadCategories();
    }
  }, [isReady]);

  const buildQuery = useCallback(() => {
    const params = new URLSearchParams();
    if (selectedCategory) params.set("category", selectedCategory.name);
    if (searchQuery.trim()) params.set("location", searchQuery.trim());
    if (filters.minPrice) params.set("minPrice", filters.minPrice);
    if (filters.maxPrice) params.set("maxPrice", filters.maxPrice);
    if (filters.status) params.set("status", filters.status);

    // Dynamic attributes
    Object.entries(filters.attributes).forEach(([key, value]) => {
      if (value.trim()) {
        params.set(`attr[${key}]`, value.trim());
      }
    });

    params.set("sortBy", filters.sortBy);
    params.set("sortDir", filters.sortDir);
    return params.toString();
  }, [selectedCategory, searchQuery, filters]);

  useEffect(() => {
    let count = 0;
    if (filters.minPrice) count++;
    if (filters.maxPrice) count++;
    if (filters.status) count++;
    count += Object.keys(filters.attributes).filter(k => filters.attributes[k].trim()).length;
    if (filters.sortBy !== "createdAt" || filters.sortDir !== "DESC") count++;
    setActiveFilterCount(count);
  }, [filters]);

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    const loadProperties = async () => {
      setLoading(true);
      try {
        const query = buildQuery();
        const data = await fetchWithAuth(`/properties?${query}`);
        setProperties(data.content || data || []);
      } catch (error) {
        console.error("Failed to load properties:", error);
      } finally {
        setLoading(false);
      }
    };
    loadProperties();
  }, [isAuthenticated, buildQuery]);

  const handleCategorySelect = (category: any | null) => {
    webApp?.HapticFeedback?.impactOccurred?.("light");
    setSelectedCategory(category);
    // Reset attribute filters when category changes
    setFilters(f => ({ ...f, attributes: {} }));
  };

  const handleResetFilters = () => {
    webApp?.HapticFeedback?.impactOccurred?.("medium");
    setFilters(DEFAULT_FILTERS);
    setSearchQuery("");
    setSelectedCategory(null);
  };

  const handleApplyFilters = () => {
    webApp?.HapticFeedback?.impactOccurred?.("medium");
    setShowFilters(false);
  };

  const handleAttributeChange = (key: string, value: string) => {
    setFilters(f => ({
      ...f,
      attributes: {
        ...f.attributes,
        [key]: value
      }
    }));
  };

  if (authLoading || !isReady) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-white dark:bg-black">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-zinc-50 pb-24 dark:bg-black">
      {/* Sticky Header */}
      <div className="bg-white px-4 pt-8 pb-4 dark:bg-zinc-900">
        <div className="mb-5 flex items-center justify-between">
          <div>
            <p className="text-[10px] font-black uppercase tracking-widest text-zinc-400">Asset Market</p>
            <h1 className="mt-0.5 text-3xl font-black tracking-tight text-zinc-900 dark:text-white">Marketplace</h1>
          </div>
          <div className="flex h-11 w-11 items-center justify-center rounded-full bg-zinc-900 text-sm font-black text-white ring-2 ring-zinc-100 dark:bg-white dark:text-black dark:ring-zinc-800">
            {webApp?.initDataUnsafe?.user?.first_name?.[0] || "TG"}
          </div>
        </div>

        {/* Search + Filter Row */}
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-400" />
            <input
              type="text"
              placeholder="Search by location..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="w-full rounded-xl bg-zinc-50 py-3 pl-9 pr-9 text-sm font-semibold outline-none ring-1 ring-zinc-100 placeholder:font-normal placeholder:text-zinc-400 dark:bg-zinc-800 dark:ring-zinc-700 dark:text-white"
            />
            {searchQuery && (
              <button onClick={() => setSearchQuery("")} className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400">
                <X size={14} />
              </button>
            )}
          </div>
          <button
            onClick={() => { webApp?.HapticFeedback?.impactOccurred?.("light"); setShowFilters(true); }}
            className={`relative flex items-center justify-center rounded-xl px-3.5 py-3 transition-all active:scale-95 ${activeFilterCount > 0
              ? "bg-zinc-900 text-white dark:bg-white dark:text-black"
              : "bg-zinc-50 text-zinc-500 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:ring-zinc-700"
              }`}
          >
            <SlidersHorizontal size={17} />
            {activeFilterCount > 0 && (
              <span className="absolute -top-1.5 -right-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[8px] font-black text-white">
                {activeFilterCount}
              </span>
            )}
          </button>
        </div>

        {/* Category Scroller */}
        <div className="mt-4 -mx-4 overflow-x-auto no-scrollbar flex gap-2 px-4 pb-1">
          <button
            onClick={() => handleCategorySelect(null)}
            className={`flex-none flex items-center gap-1.5 rounded-xl px-4 py-2 text-xs font-black transition-all active:scale-95 ${selectedCategory === null
              ? "bg-zinc-900 text-white dark:bg-white dark:text-black"
              : "bg-zinc-50 text-zinc-500 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:text-zinc-400 dark:ring-zinc-700"
              }`}
          >
            <LayoutGrid size={12} />
            All
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              onClick={() => handleCategorySelect(cat)}
              className={`flex-none rounded-xl px-4 py-2 text-xs font-black transition-all active:scale-95 ${selectedCategory?.id === cat.id
                ? "bg-zinc-900 text-white dark:bg-white dark:text-black"
                : "bg-zinc-50 text-zinc-500 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:text-zinc-400 dark:ring-zinc-700"
                }`}
            >
              {cat.name}
            </button>
          ))}
          {categoriesLoading && [1, 2, 3].map(i => (
            <div key={i} className="h-8 w-20 animate-pulse rounded-xl bg-zinc-100 dark:bg-zinc-800 flex-none" />
          ))}
        </div>
      </div>

      {/* Results */}
      <div className="px-4 pt-5">
        {(activeFilterCount > 0 || selectedCategory || searchQuery) && (
          <div className="mb-3 flex items-center justify-between">
            <p className="text-xs font-bold text-zinc-500">
              {loading ? "Searching..." : `${properties.length} result${properties.length !== 1 ? "s" : ""}`}
            </p>
            <button onClick={handleResetFilters} className="text-[10px] font-black uppercase tracking-widest text-red-500">
              Clear All
            </button>
          </div>
        )}

        {loading ? (
          <div className="space-y-3">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="flex gap-4 rounded-2xl bg-white p-3 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                <div className="h-28 w-28 flex-shrink-0 animate-pulse rounded-xl bg-zinc-100 dark:bg-zinc-800" />
                <div className="flex-1 space-y-2 py-1">
                  <div className="h-2.5 w-16 animate-pulse rounded-full bg-zinc-100 dark:bg-zinc-800" />
                  <div className="h-4 w-3/4 animate-pulse rounded-full bg-zinc-100 dark:bg-zinc-800" />
                  <div className="h-2.5 w-1/2 animate-pulse rounded-full bg-zinc-100 dark:bg-zinc-800" />
                  <div className="mt-4 h-4 w-1/3 animate-pulse rounded-full bg-zinc-100 dark:bg-zinc-800" />
                </div>
              </div>
            ))}
          </div>
        ) : properties.length === 0 ? (
          <div className="mt-20 text-center">
            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-zinc-100 dark:bg-zinc-900">
              <Search size={28} className="text-zinc-300 dark:text-zinc-600" />
            </div>
            <p className="mt-5 text-base font-black text-zinc-900 dark:text-white">No properties found</p>
            <p className="text-sm text-zinc-500">Try adjusting your filters</p>
            {(activeFilterCount > 0 || selectedCategory || searchQuery) && (
              <button
                onClick={handleResetFilters}
                className="mt-4 rounded-full bg-zinc-900 px-6 py-2.5 text-xs font-black text-white dark:bg-white dark:text-black"
              >
                Reset Filters
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            {properties.map((property) => (
              <PropertyCard key={property.id} property={property} />
            ))}
          </div>
        )}
      </div>

      {/* Filter Sheet */}
      {showFilters && (
        <div className="fixed inset-0 z-[100] flex flex-col justify-end">
          <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setShowFilters(false)} />
          <div className="relative z-10 max-h-[90vh] overflow-y-auto rounded-t-3xl bg-white px-6 pt-4 pb-32 shadow-2xl dark:bg-zinc-900">
            <div className="mx-auto mb-6 h-1 w-10 rounded-full bg-zinc-200 dark:bg-zinc-700" />
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-black text-zinc-900 dark:text-white">Filters & Sort</h2>
              {activeFilterCount > 0 && (
                <button onClick={() => setFilters(DEFAULT_FILTERS)} className="text-[10px] font-black uppercase tracking-widest text-red-500">
                  Reset
                </button>
              )}
            </div>

            <div className="space-y-6">
              {/* Sort */}
              <div>
                <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400">Sort By</label>
                <div className="mt-2 grid grid-cols-3 gap-2">
                  {[
                    { label: "Newest", sortBy: "createdAt", sortDir: "DESC" },
                    { label: "Price ↑", sortBy: "price", sortDir: "ASC" },
                    { label: "Price ↓", sortBy: "price", sortDir: "DESC" },
                  ].map(opt => (
                    <button
                      key={opt.label}
                      type="button"
                      onClick={() => setFilters(f => ({ ...f, sortBy: opt.sortBy, sortDir: opt.sortDir }))}
                      className={`rounded-xl py-2.5 text-xs font-black transition-all ${filters.sortBy === opt.sortBy && filters.sortDir === opt.sortDir
                        ? "bg-zinc-900 text-white dark:bg-white dark:text-black"
                        : "bg-zinc-50 text-zinc-500 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:ring-zinc-700"
                        }`}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Status */}
              <div>
                <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400">Status</label>
                <div className="mt-2 grid grid-cols-4 gap-2">
                  {["", "AVAILABLE", "PENDING", "SOLD"].map(s => (
                    <button
                      key={s}
                      type="button"
                      onClick={() => setFilters(f => ({ ...f, status: s }))}
                      className={`rounded-xl py-2.5 text-[10px] font-black transition-all ${filters.status === s
                        ? "bg-zinc-900 text-white dark:bg-white dark:text-black"
                        : "bg-zinc-50 text-zinc-500 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:ring-zinc-700"
                        }`}
                    >
                      {s || "Any"}
                    </button>
                  ))}
                </div>
              </div>

              {/* Price Range */}
              <div>
                <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400">Price Range</label>
                <div className="mt-2 flex gap-3">
                  <div className="flex-1 rounded-2xl bg-zinc-50 px-4 py-3 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:ring-zinc-700">
                    <p className="text-[8px] font-black uppercase tracking-widest text-zinc-400">Min</p>
                    <input
                      type="number"
                      placeholder="0"
                      value={filters.minPrice}
                      onChange={e => setFilters(f => ({ ...f, minPrice: e.target.value }))}
                      className="w-full bg-transparent text-sm font-black outline-none dark:text-white"
                    />
                  </div>
                  <div className="flex-1 rounded-2xl bg-zinc-50 px-4 py-3 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:ring-zinc-700">
                    <p className="text-[8px] font-black uppercase tracking-widest text-zinc-400">Max</p>
                    <input
                      type="number"
                      placeholder="Any"
                      value={filters.maxPrice}
                      onChange={e => setFilters(f => ({ ...f, maxPrice: e.target.value }))}
                      className="w-full bg-transparent text-sm font-black outline-none dark:text-white"
                    />
                  </div>
                </div>
              </div>

              {/* Dynamic Attributes */}
              {selectedCategory?.attributeSchema && selectedCategory.attributeSchema.length > 0 && (
                <div className="space-y-4">
                  <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400">
                    {selectedCategory.name} Details
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    {selectedCategory.attributeSchema.map((attr: any) => (
                      <div key={attr.name} className="rounded-2xl bg-zinc-50 px-4 py-3 ring-1 ring-zinc-100 dark:bg-zinc-800 dark:ring-zinc-700">
                        <p className="text-[8px] font-black uppercase tracking-widest text-zinc-400">
                          {attr.name}
                        </p>
                        {attr.type === 'boolean' ? (
                          <div className="mt-1 flex gap-2">
                            {['', 'true', 'false'].map(val => (
                              <button
                                key={val}
                                onClick={() => handleAttributeChange(attr.name, val)}
                                className={`px-2 py-1 rounded-md text-[9px] font-black ${filters.attributes[attr.name] === val
                                  ? 'bg-zinc-900 text-white dark:bg-white dark:text-black'
                                  : 'bg-zinc-200 text-zinc-600 dark:bg-zinc-700'}`}
                              >
                                {val === '' ? 'Any' : val === 'true' ? 'Yes' : 'No'}
                              </button>
                            ))}
                          </div>
                        ) : (
                          <input
                            type={attr.type === 'number' ? 'number' : 'text'}
                            placeholder={attr.type === 'number' ? '0' : '...'}
                            value={filters.attributes[attr.name] || ''}
                            onChange={e => handleAttributeChange(attr.name, e.target.value)}
                            className="w-full bg-transparent text-sm font-black outline-none dark:text-white"
                          />
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <button
              onClick={handleApplyFilters}
              className="mt-8 w-full rounded-2xl bg-zinc-900 py-4 text-base font-black text-white shadow-xl active:scale-[0.98] dark:bg-white dark:text-black"
            >
              Apply Filters
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
