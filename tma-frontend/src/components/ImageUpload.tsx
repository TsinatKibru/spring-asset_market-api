"use client";

import { useState, useRef } from "react";
import { Plus, X, Image as ImageIcon, Loader2 } from "lucide-react";
import Image from "next/image";
import { useTelegram } from "./providers/TelegramProvider";

interface ImageUploadProps {
    value: string[];
    onChange: (urls: string[]) => void;
}

export default function ImageUpload({ value, onChange }: ImageUploadProps) {
    const { webApp } = useTelegram();
    const [uploading, setUploading] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files;
        if (!files || files.length === 0) return;

        setUploading(true);
        webApp.HapticFeedback.impactOccurred('light');

        const newUrls: string[] = [...value];
        const token = localStorage.getItem("token");
        const tenantId = webApp.initDataUnsafe?.start_param || "default";

        try {
            for (let i = 0; i < files.length; i++) {
                const formData = new FormData();
                formData.append("file", files[i]);

                const response = await fetch("/api/v1/properties/upload", {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        "X-Tenant-ID": tenantId
                    },
                    body: formData
                });

                if (!response.ok) throw new Error("Upload failed");

                const data = await response.json();
                newUrls.push(data.url);
            }

            onChange(newUrls);
            webApp.HapticFeedback.notificationOccurred('success');
        } catch (error) {
            console.error("Upload error:", error);
            webApp.showAlert("Failed to upload one or more images.");
        } finally {
            setUploading(false);
            if (fileInputRef.current) fileInputRef.current.value = "";
        }
    };

    const removeImage = (index: number) => {
        webApp.HapticFeedback.impactOccurred('medium');
        const newUrls = [...value];
        newUrls.splice(index, 1);
        onChange(newUrls);
    };

    return (
        <div className="space-y-4">
            <label className="text-[10px] font-black uppercase tracking-widest text-zinc-400 ml-1">
                Property Images
            </label>

            <div className="flex flex-wrap gap-4">
                {/* Previews */}
                {value.map((url, index) => (
                    <div
                        key={index}
                        className="relative h-24 w-24 overflow-hidden rounded-2xl ring-1 ring-zinc-100 dark:ring-zinc-800"
                    >
                        <Image
                            src={url}
                            alt={`Upload ${index}`}
                            fill
                            className="object-cover"
                        />
                        <button
                            type="button"
                            onClick={() => removeImage(index)}
                            className="absolute top-1 right-1 flex h-6 w-6 items-center justify-center rounded-full bg-black/50 text-white backdrop-blur-sm transition-transform active:scale-90"
                        >
                            <X size={14} />
                        </button>
                    </div>
                ))}

                {/* Upload Button */}
                <button
                    type="button"
                    disabled={uploading}
                    onClick={() => fileInputRef.current?.click()}
                    className="flex h-24 w-24 flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-zinc-200 bg-white transition-all active:scale-95 dark:border-zinc-800 dark:bg-zinc-900"
                >
                    {uploading ? (
                        <Loader2 className="animate-spin text-zinc-400" size={24} />
                    ) : (
                        <>
                            <Plus size={24} className="text-zinc-400" />
                            <span className="text-[10px] font-bold text-zinc-400 uppercase">Add</span>
                        </>
                    )}
                </button>
            </div>

            <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                multiple
                accept="image/*"
                className="hidden"
            />

            {value.length === 0 && !uploading && (
                <div className="flex items-center gap-2 rounded-2xl bg-zinc-50 p-4 dark:bg-zinc-900/50">
                    <ImageIcon size={16} className="text-zinc-400" />
                    <p className="text-xs text-zinc-500">
                        High-quality photos increase buyer interest by 40%.
                    </p>
                </div>
            )}
        </div>
    );
}
