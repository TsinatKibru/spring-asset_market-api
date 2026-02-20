"use client";

import { useState, useRef, useEffect } from "react";
import Image from "next/image";

interface ImageCarouselProps {
    images: string[];
    title: string;
}

export default function ImageCarousel({ images, title }: ImageCarouselProps) {
    const [currentIndex, setCurrentIndex] = useState(0);
    const scrollRef = useRef<HTMLDivElement>(null);

    const handleScroll = () => {
        if (!scrollRef.current) return;
        const index = Math.round(scrollRef.current.scrollLeft / scrollRef.current.offsetWidth);
        setCurrentIndex(index);
    };

    if (!images || images.length === 0) {
        return (
            <div className="flex h-full w-full items-center justify-center bg-zinc-100 dark:bg-zinc-900">
                <span className="text-zinc-400 tracking-widest uppercase text-[10px] font-black">No Preview Available</span>
            </div>
        );
    }

    return (
        <div className="relative h-full w-full group">
            <div
                ref={scrollRef}
                onScroll={handleScroll}
                className="flex h-full w-full overflow-x-auto snap-x snap-mandatory scrollbar-hide"
                style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
            >
                {images.map((url, index) => (
                    <div
                        key={index}
                        className="relative h-full w-full flex-shrink-0 snap-start"
                    >
                        <Image
                            src={url}
                            alt={`${title} - image ${index + 1}`}
                            fill
                            className="object-cover"
                            priority={index === 0}
                        />
                    </div>
                ))}
            </div>

            {/* Pagination Dots */}
            {images.length > 1 && (
                <div className="absolute bottom-6 left-0 right-0 flex justify-center gap-2 px-6">
                    {images.map((_, index) => (
                        <div
                            key={index}
                            className={`h-1.5 rounded-full transition-all duration-300 ${currentIndex === index
                                    ? "w-8 bg-white"
                                    : "w-1.5 bg-white/40"
                                }`}
                        />
                    ))}
                </div>
            )}

            {/* Image Count Badge */}
            {images.length > 1 && (
                <div className="absolute top-6 right-6 rounded-full bg-black/30 px-3 py-1.5 backdrop-blur-md">
                    <p className="text-[10px] font-black text-white uppercase tracking-tighter">
                        {currentIndex + 1} / {images.length}
                    </p>
                </div>
            )}
        </div>
    );
}
