// Internal Imports
import Vue from 'vue'

try {
    window.Popper = require('popper.js').default;
    window.$ = window.jQuery = require('jquery');

    require('bootstrap');
} catch (e) {

}
window.axios = require('axios');
window.axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';


Vue.component('my-component', require('./components/MyComponent').default);
Vue.component('test-component', require('./components/TestComponent').default);
Vue.component('bot-component', require('./components/BotComponent').default);

// Instantiate
const app = new Vue({
    el: '#app',
});