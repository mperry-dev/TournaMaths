module.exports = {
  env: {
    browser: true,
    es2021: true,
  },
  extends: [
    'eslint:recommended',
  ],
  parserOptions: {
    ecmaVersion: 12, // ECMAScript 2021
    sourceType: 'script',
  },
  globals: {
    $: 'readonly', // For JQuery
    jQuery: 'readonly',
    katex: 'readonly',
  },
  rules: {
    // your custom rules here
  },
};
