// Internal Imports
import Vue from 'vue'

try {
    window.Popper = require('popper.js').default;
    window.$ = window.jQuery = require('jquery');

    require('bootstrap');
} catch (e) {

}

import { library } from '@fortawesome/fontawesome-svg-core'
import {faToggleOff, faToggleOn, faCircle, faRedoAlt, faPowerOff, faExternalLinkAlt, faFileAlt, faSignInAlt} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faToggleOff, faToggleOn, faCircle, faRedoAlt, faPowerOff, faExternalLinkAlt, faFileAlt, faSignInAlt)
Vue.component('font-awesome-icon', FontAwesomeIcon)


window.axios = require('axios');
window.axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';


Vue.component('toggle-component', require('./components/ToggleComponent').default);
Vue.component('navigation-component', require('./components/NavigationComponent').default);
Vue.component('bot-component', require('./components/BotComponent').default);
Vue.component('login-component', require('./components/LoginComponent').default);
Vue.component('reset-component', require('./components/ResetPasswordComponent').default);

// Instantiate
const app = new Vue({
    el: '#app',
});