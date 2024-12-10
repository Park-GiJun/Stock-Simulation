module.exports = {
    apps: [
        {
            name: 'frontend', // PM2에서 사용할 애플리케이션 이름
            script: 'node_modules/vite/bin/vite.js',
            args: 'preview', // 빌드된 애플리케이션을 미리보기 모드로 실행
            env: {
                PORT: 8080,
                NODE_ENV: 'production'
            }
        }
    ]
};
