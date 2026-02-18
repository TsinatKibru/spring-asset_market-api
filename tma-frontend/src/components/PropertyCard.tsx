"use client";

import Image from "next/image";

import Link from "next/link";
import { Star, Bed, Bath, Maximize } from "lucide-react";

interface Property {
    id: number;
    title: string;
    price: number;
    location: string;
    imageUrls: string[];
    categoryName: string;
    averageRating: number | null;
    reviewCount: number;
    attributes?: Record<string, any>;
}

export default function PropertyCard({ property }: { property: Property }) {
    // Extract common specs for quick display
    const specs = property.attributes || {};
    const hasSpecs = specs.bedrooms || specs.bathrooms || specs.sqft || specs.floor;

    return (
        <div className="group overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-zinc-200 transition-all hover:shadow-md dark:bg-zinc-900 dark:ring-zinc-800">
            <Link href={`/property/${property.id}`}>
                <div className="relative aspect-[4/3] w-full overflow-hidden">
                    {property.imageUrls && property.imageUrls.length > 0 ? (
                        <Image
                            src={property.imageUrls[0]}
                            alt={property.title}
                            fill
                            className="object-cover transition-transform duration-500 group-hover:scale-110"
                        />
                    ) : (
                        <div className="flex h-full w-full items-center justify-center bg-zinc-100 dark:bg-zinc-800">
                            <span className="text-zinc-400">No Image</span>
                        </div>
                    )}
                    <div className="absolute top-3 left-3 rounded-full bg-white/90 px-3 py-1 text-xs font-semibold text-zinc-900 backdrop-blur-sm dark:bg-black/80 dark:text-white">
                        {property.categoryName}
                    </div>
                </div>
            </Link>

            <div className="p-4">
                <div className="flex items-start justify-between">
                    <Link href={`/property/${property.id}`} className="flex-1">
                        <h3 className="line-clamp-1 text-lg font-bold text-zinc-900 dark:text-white hover:text-zinc-700 dark:hover:text-zinc-300">
                            {property.title}
                        </h3>
                    </Link>
                    {property.averageRating && (
                        <div className="flex items-center gap-1 text-sm font-medium text-amber-500">
                            <Star size={14} fill="currentColor" />
                            <span>{property.averageRating.toFixed(1)}</span>
                        </div>
                    )}
                </div>

                <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                    {property.location}
                </p>

                {/* Quick Specs Row */}
                {hasSpecs && (
                    <div className="mt-3 flex items-center gap-3 border-t border-zinc-50 pt-3 dark:border-zinc-800">
                        {specs.bedrooms && (
                            <div className="flex items-center gap-1 text-[10px] font-bold text-zinc-400">
                                <Bed size={12} strokeWidth={2.5} />
                                <span>{specs.bedrooms}</span>
                            </div>
                        )}
                        {specs.bathrooms && (
                            <div className="flex items-center gap-1 text-[10px] font-bold text-zinc-400">
                                <Bath size={12} strokeWidth={2.5} />
                                <span>{specs.bathrooms}</span>
                            </div>
                        )}
                        {(specs.sqft || specs.floor) && (
                            <div className="flex items-center gap-1 text-[10px] font-bold text-zinc-400">
                                <Maximize size={12} strokeWidth={2.5} />
                                <span>{specs.sqft || specs.floor}</span>
                            </div>
                        )}
                    </div>
                )}

                <div className="mt-4 flex items-center justify-between">
                    <div className="text-xl font-black text-zinc-900 dark:text-white">
                        ${property.price.toLocaleString()}
                    </div>
                    <Link
                        href={`/property/${property.id}`}
                        className="rounded-full bg-zinc-900 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-zinc-800 dark:bg-white dark:text-black dark:hover:bg-zinc-100"
                    >
                        View
                    </Link>
                </div>
            </div>
        </div>
    );
}
