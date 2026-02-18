"use client";

export default function MessagesPage() {
    return (
        <div className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6 text-center dark:bg-black">
            <span className="text-5xl">💬</span>
            <h1 className="mt-6 text-2xl font-black text-zinc-900 dark:text-white">Messages</h1>
            <p className="mt-2 text-zinc-500 dark:text-zinc-400 max-w-xs">
                Your inquiries and chat history with agents will appear here. This feature is coming in Phase 19!
            </p>
        </div>
    );
}
