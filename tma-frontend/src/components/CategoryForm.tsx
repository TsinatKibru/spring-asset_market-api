"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useApi } from "@/components/hooks/useApi";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { Plus, Trash2, Save, X, Settings2, PlusCircle, CheckCircle2 } from "lucide-react";

interface AttributeSchema {
    name: string;
    type: "string" | "number" | "boolean";
    required: boolean;
}

interface CategoryFormProps {
    initialData?: {
        name: string;
        description: string;
        attributeSchema: AttributeSchema[];
    };
    id?: string;
}

export default function CategoryForm({ initialData, id }: CategoryFormProps) {
    const router = useRouter();
    const { fetchWithAuth } = useApi();
    const { webApp } = useTelegram();

    const [formData, setFormData] = useState({
        name: initialData?.name || "",
        description: initialData?.description || "",
        attributeSchema: initialData?.attributeSchema || [],
    });

    const [submitting, setSubmitting] = useState(false);

    const handleAddAttribute = () => {
        webApp.HapticFeedback.impactOccurred("light");
        setFormData({
            ...formData,
            attributeSchema: [
                ...formData.attributeSchema,
                { name: "", type: "string", required: false },
            ],
        });
    };

    const handleRemoveAttribute = (index: number) => {
        webApp.HapticFeedback.impactOccurred("medium");
        const newSchema = [...formData.attributeSchema];
        newSchema.splice(index, 1);
        setFormData({ ...formData, attributeSchema: newSchema });
    };

    const handleAttributeChange = (index: number, field: keyof AttributeSchema, value: any) => {
        const newSchema = [...formData.attributeSchema];
        newSchema[index] = { ...newSchema[index], [field]: value };
        setFormData({ ...formData, attributeSchema: newSchema });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (submitting) return;

        // Validation
        if (!formData.name.trim()) {
            webApp.showAlert("Please specify a category name.");
            return;
        }

        if (formData.attributeSchema.some(a => !a.name.trim())) {
            webApp.showAlert("All attributes must have a name.");
            return;
        }

        webApp.HapticFeedback.impactOccurred("medium");
        setSubmitting(true);

        try {
            const url = id ? `/categories/${id}` : "/categories";
            const method = id ? "PUT" : "POST";

            await fetchWithAuth(url, {
                method,
                body: JSON.stringify(formData),
            });

            webApp.showConfirm(id ? "Category updated!" : "Category created!");
            router.push("/merchant/categories");
        } catch (error: any) {
            webApp.showAlert(error.message || "Failed to save category.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="min-h-screen bg-zinc-50 pb-32 dark:bg-black">
            <header className="bg-white px-6 pt-8 pb-6 shadow-sm dark:bg-zinc-900 border-b border-zinc-100 dark:border-zinc-800">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-black tracking-tight text-zinc-900 dark:text-white">
                            {id ? "Edit Type" : "New Asset Type"}
                        </h1>
                        <p className="text-[10px] font-black uppercase tracking-widest text-zinc-400">
                            Schema Configurator
                        </p>
                    </div>
                </div>
            </header>

            <div className="px-6 pt-8 space-y-8">
                {/* Basic Info */}
                <section className="space-y-4">
                    <div className="relative">
                        <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400 ml-1">Name</label>
                        <div className="mt-1 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                            <input
                                required
                                type="text"
                                placeholder="e.g., Luxury Villas"
                                className="w-full bg-transparent text-sm font-bold outline-none dark:text-white"
                                value={formData.name}
                                onChange={e => setFormData({ ...formData, name: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="relative">
                        <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400 ml-1">Description</label>
                        <div className="mt-1 rounded-2xl bg-white p-4 ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                            <textarea
                                rows={3}
                                placeholder="Describe this asset type..."
                                className="w-full bg-transparent text-sm font-bold outline-none dark:text-white resize-none"
                                value={formData.description}
                                onChange={e => setFormData({ ...formData, description: e.target.value })}
                            />
                        </div>
                    </div>
                </section>

                {/* Attribute Schema Builder */}
                <section>
                    <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-2">
                            <Settings2 size={16} className="text-zinc-400" />
                            <h3 className="text-xs font-black uppercase tracking-widest text-zinc-900 dark:text-white">Detailed Specs</h3>
                        </div>
                        <button
                            type="button"
                            onClick={handleAddAttribute}
                            className="flex items-center gap-1 text-[10px] font-black uppercase tracking-widest text-zinc-500 hover:text-zinc-900 dark:hover:text-white"
                        >
                            <PlusCircle size={14} /> Add Field
                        </button>
                    </div>

                    <div className="space-y-4">
                        {formData.attributeSchema.length === 0 ? (
                            <div className="rounded-3xl border-2 border-dashed border-zinc-100 p-10 text-center dark:border-zinc-800">
                                <PlusCircle size={32} className="mx-auto text-zinc-200 dark:text-zinc-700" />
                                <p className="mt-4 text-[10px] font-black uppercase tracking-widest text-zinc-400">No Custom Fields Yet</p>
                                <p className="mt-1 text-xs text-zinc-400">Tap "Add Field" to define what data this asset type collects</p>
                                <button
                                    type="button"
                                    onClick={handleAddAttribute}
                                    className="mt-4 text-xs font-black text-zinc-900 underline dark:text-white"
                                >
                                    Add your first field
                                </button>
                            </div>
                        ) : (
                            formData.attributeSchema.map((attr, index) => (
                                <div
                                    key={index}
                                    className="relative rounded-3xl bg-white p-6 shadow-sm ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800"
                                >
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveAttribute(index)}
                                        className="absolute top-4 right-4 text-zinc-300 hover:text-red-500"
                                    >
                                        <X size={18} />
                                    </button>

                                    <div className="space-y-4">
                                        <div className="relative">
                                            <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400 ml-1">Field Name</label>
                                            <input
                                                type="text"
                                                placeholder="e.g., squareFootage, hasPool, yearBuilt"
                                                className="mt-1 w-full border-b border-zinc-100 bg-transparent pb-1 text-sm font-black outline-none dark:border-zinc-800 dark:text-white"
                                                value={attr.name}
                                                onChange={e => handleAttributeChange(index, "name", e.target.value)}
                                            />
                                        </div>

                                        <div className="flex gap-4">
                                            <div className="flex-1">
                                                <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400 ml-1">Type</label>
                                                <select
                                                    className="mt-1 w-full bg-transparent text-xs font-bold outline-none dark:text-white"
                                                    value={attr.type}
                                                    onChange={e => handleAttributeChange(index, "type", e.target.value as any)}
                                                >
                                                    <option value="string">Text</option>
                                                    <option value="number">Number</option>
                                                    <option value="boolean">Yes/No</option>
                                                </select>
                                            </div>
                                            <div className="flex items-center gap-3 pr-8">
                                                <div className="flex flex-col items-end">
                                                    <label className="text-[8px] font-black uppercase tracking-widest text-zinc-400">Required</label>
                                                    <div
                                                        onClick={() => handleAttributeChange(index, "required", !attr.required)}
                                                        className={`mt-1 h-5 w-10 rounded-full transition-colors cursor-pointer ${attr.required ? 'bg-zinc-900 dark:bg-white' : 'bg-zinc-100 dark:bg-zinc-800'}`}
                                                    >
                                                        <div className={`h-4 w-4 m-0.5 rounded-full bg-white dark:bg-black transition-transform ${attr.required ? 'translate-x-5' : 'translate-x-0'}`} />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </section>
            </div>

            <div className="fixed bottom-24 left-0 right-0 px-6 py-4 backdrop-blur-md bg-white/50 dark:bg-black/50">
                <button
                    type="submit"
                    disabled={submitting}
                    className={`flex w-full items-center justify-center gap-2 rounded-2xl py-5 text-lg font-black transition-transform active:scale-[0.98] shadow-xl ${submitting ? "bg-zinc-400 cursor-not-allowed" : "bg-zinc-900 text-white dark:bg-white dark:text-black"
                        }`}
                >
                    <Save size={20} />
                    {submitting ? "Saving..." : "Save Config"}
                </button>
            </div>
        </form>
    );
}
