"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useApi } from "@/components/hooks/useApi";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { Save, List, MapPin, DollarSign, FileText } from "lucide-react";

export default function EditPropertyPage() {
    const { id } = useParams();
    const router = useRouter();
    const { fetchWithAuth, postWithAuth } = useApi();
    const { webApp } = useTelegram();

    const [categories, setCategories] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    const [formData, setFormData] = useState({
        title: "",
        location: "",
        price: "",
        categoryName: "",
        description: "",
        status: "AVAILABLE",
        attributes: {} as any,
        imageUrls: [] as string[]
    });

    const [selectedCategory, setSelectedCategory] = useState<any>(null);

    useEffect(() => {
        webApp.BackButton.show();
        webApp.BackButton.onClick(() => router.back());

        const loadData = async () => {
            try {
                const [cats, property] = await Promise.all([
                    fetchWithAuth("/categories"),
                    fetchWithAuth(`/properties/${id}`)
                ]);

                setCategories(cats);
                setFormData({
                    title: property.title,
                    location: property.location,
                    price: property.price.toString(),
                    categoryName: property.categoryName,
                    description: property.description || "",
                    status: property.status,
                    attributes: property.attributes || {},
                    imageUrls: property.imageUrls || []
                });

                const cat = cats.find((c: any) => c.name === property.categoryName);
                setSelectedCategory(cat);
            } catch (error) {
                console.error("Failed to load data:", error);
                webApp.showAlert("Failed to load property details.");
            } finally {
                setLoading(false);
            }
        };

        loadData();

        return () => {
            webApp.BackButton.hide();
        };
    }, [id]);

    const handleCategoryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const catName = e.target.value;
        const cat = categories.find(c => c.name === catName);
        setSelectedCategory(cat);
        setFormData(prev => ({
            ...prev,
            categoryName: catName,
            attributes: {}
        }));
    };

    const handleAttributeChange = (name: string, value: any) => {
        setFormData(prev => ({
            ...prev,
            attributes: { ...prev.attributes, [name]: value }
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (submitting) return;

        // Prepare attributes based on schema
        const processedAttributes = { ...formData.attributes };
        if (selectedCategory?.attributeSchema) {
            selectedCategory.attributeSchema.forEach((attr: any) => {
                const val = processedAttributes[attr.name];
                if (val !== undefined && val !== "") {
                    if (attr.type === 'number') {
                        processedAttributes[attr.name] = parseFloat(val);
                    } else if (attr.type === 'boolean') {
                        processedAttributes[attr.name] = val === true || val === "true" || val === "1" || val === "yes";
                    }
                }
            });
        }

        webApp.HapticFeedback.impactOccurred('medium');
        setSubmitting(true);

        try {
            await fetchWithAuth(`/properties/${id}`, {
                method: "PUT",
                body: JSON.stringify({
                    ...formData,
                    price: parseFloat(formData.price),
                    attributes: processedAttributes
                })
            });
            webApp.showConfirm("Changes saved successfully!");
            router.push("/merchant/dashboard");
        } catch (error: any) {
            webApp.showAlert(error.message || "Failed to update property.");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-white dark:bg-black">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-zinc-50 pb-32 dark:bg-black">
            {/* Header */}
            <div className="bg-white px-6 pt-12 pb-8 shadow-sm dark:bg-zinc-900">
                <h1 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-white">Edit Listing</h1>
                <p className="mt-2 text-zinc-500 dark:text-zinc-400">Update your property details</p>
            </div>

            <form onSubmit={handleSubmit} className="px-6 mt-8 space-y-6">
                <div className="space-y-4">
                    <div className="relative">
                        <label className="text-[10px] font-black uppercase tracking-widest text-zinc-400 ml-1">Property Title</label>
                        <div className="mt-1 flex items-center gap-3 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                            <FileText size={18} className="text-zinc-400" />
                            <input
                                required
                                type="text"
                                className="flex-1 bg-transparent text-sm font-bold outline-none dark:text-white"
                                value={formData.title}
                                onChange={e => setFormData({ ...formData, title: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="relative">
                        <label className="text-[10px] font-black uppercase tracking-widest text-zinc-400 ml-1">Location</label>
                        <div className="mt-1 flex items-center gap-3 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                            <MapPin size={18} className="text-zinc-400" />
                            <input
                                required
                                type="text"
                                className="flex-1 bg-transparent text-sm font-bold outline-none dark:text-white"
                                value={formData.location}
                                onChange={e => setFormData({ ...formData, location: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="relative">
                            <label className="text-[10px] font-black uppercase tracking-widest text-zinc-400 ml-1">Price ($)</label>
                            <div className="mt-1 flex items-center gap-3 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                                <DollarSign size={18} className="text-zinc-400" />
                                <input
                                    required
                                    type="number"
                                    className="flex-1 bg-transparent text-sm font-bold outline-none dark:text-white"
                                    value={formData.price}
                                    onChange={e => setFormData({ ...formData, price: e.target.value })}
                                />
                            </div>
                        </div>
                        <div className="relative">
                            <label className="text-[10px] font-black uppercase tracking-widest text-zinc-400 ml-1">Category</label>
                            <div className="mt-1 flex items-center gap-3 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                                <List size={18} className="text-zinc-400" />
                                <select
                                    required
                                    className="flex-1 bg-transparent text-sm font-bold outline-none dark:text-white appearance-none"
                                    value={formData.categoryName}
                                    onChange={handleCategoryChange}
                                >
                                    {categories.map(c => (
                                        <option key={c.id} value={c.name}>{c.name}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                {selectedCategory && selectedCategory.attributeSchema && selectedCategory.attributeSchema.length > 0 && (
                    <div className="space-y-4 pt-4 border-t border-zinc-100 dark:border-zinc-800">
                        <h3 className="text-xs font-black uppercase tracking-widest text-zinc-900 dark:text-white">Specifications</h3>
                        <div className="grid grid-cols-2 gap-4">
                            {selectedCategory.attributeSchema.map((attr: any) => (
                                <div key={attr.name} className="relative">
                                    <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400 ml-1">{attr.name}</label>
                                    <div className="mt-1 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                                        {attr.type === 'boolean' ? (
                                            <div
                                                onClick={() => handleAttributeChange(attr.name, !formData.attributes[attr.name])}
                                                className="flex items-center justify-between cursor-pointer"
                                            >
                                                <span className="text-sm font-bold dark:text-white">
                                                    {formData.attributes[attr.name] ? 'Yes' : 'No'}
                                                </span>
                                                <div className={`h-5 w-10 rounded-full transition-colors ${formData.attributes[attr.name] ? 'bg-zinc-900 dark:bg-white' : 'bg-zinc-200 dark:bg-zinc-700'}`}>
                                                    <div className={`h-4 w-4 m-0.5 rounded-full bg-white dark:bg-black transition-transform ${formData.attributes[attr.name] ? 'translate-x-5' : 'translate-x-0'}`} />
                                                </div>
                                            </div>
                                        ) : (
                                            <input
                                                required={attr.required}
                                                type={attr.type === 'number' ? 'number' : 'text'}
                                                className="w-full bg-transparent text-sm font-bold outline-none dark:text-white"
                                                value={formData.attributes[attr.name] || ""}
                                                onChange={e => handleAttributeChange(attr.name, e.target.value)}
                                            />
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                <div className="relative">
                    <label className="text-[10px] font-black uppercase tracking-widest text-zinc-400 ml-1">Full Description</label>
                    <div className="mt-1 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                        <textarea
                            rows={4}
                            className="w-full bg-transparent text-sm font-bold outline-none dark:text-white resize-none"
                            value={formData.description}
                            onChange={e => setFormData({ ...formData, description: e.target.value })}
                        />
                    </div>
                </div>

                <div className="fixed bottom-12 left-0 right-0 px-6 backdrop-blur-md bg-white/50 dark:bg-black/50 py-4">
                    <button
                        type="submit"
                        disabled={submitting}
                        className={`w-full flex items-center justify-center gap-3 rounded-2xl py-5 text-lg font-black transition-all active:scale-[0.98] shadow-xl ${submitting
                            ? "bg-zinc-400 cursor-not-allowed"
                            : "bg-zinc-900 text-white dark:bg-white dark:text-black"
                            }`}
                    >
                        {submitting ? (
                            <div className="h-5 w-5 animate-spin rounded-full border-2 border-zinc-200 border-t-white dark:border-zinc-800 dark:border-t-black" />
                        ) : (
                            <>
                                <Save size={20} /> Update Listing
                            </>
                        )}
                    </button>
                </div>
            </form>
        </div>
    );
}
