<template>

  <router-view/>
</template>
<style>
@import '@/styles/variables.css';

*,
*::before,
*::after {
  box-sizing: border-box;
}

html,body,div,p,span,h1,h2,h3,h4,h5,h6,ul,ol,li,input{
  margin: 0;
  padding: 0;
}
html,body,#app{
  width: 100%;
  height: 100%;
  font-family: var(--font-family);
}
ul,ol{
  list-style: none;
}
a{
  text-decoration: none;
}
</style>
