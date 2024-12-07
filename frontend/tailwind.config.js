/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: '#4B5563', // gray-600
                secondary: '#9CA3AF', // gray-400
                accent: '#1F2937', // gray-800
            }
        },
    },
    plugins: [],
}