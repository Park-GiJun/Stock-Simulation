// src/store/user.js
import { ref, readonly } from 'vue';

const userId = ref('');

export const useUserStore = () => {
    const setUserId = (id) => {
        userId.value = id;
    };

    const clearUserId = () => {
        userId.value = '';
    };

    return {
        userId: readonly(userId),
        setUserId,
        clearUserId
    };
};