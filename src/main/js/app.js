// Internal Imports
import Vue from 'vue'
import {library} from '@fortawesome/fontawesome-svg-core'
import {
    faCircle,
    faExternalLinkAlt,
    faFileAlt,
    faLevelUpAlt,
    faPlusSquare,
    faPowerOff,
    faRedoAlt,
    faSignInAlt,
    faToggleOff,
    faToggleOn
} from '@fortawesome/free-solid-svg-icons'

import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import VueSweetalert2 from 'vue-sweetalert2';
import 'sweetalert2/dist/sweetalert2.min.css';

try {
    window.Popper = require('popper.js').default;
    window.$ = window.jQuery = require('jquery');

    require('bootstrap');
} catch (e) {

}

library.add(faToggleOff, faToggleOn, faCircle, faRedoAlt, faPowerOff,
    faExternalLinkAlt, faFileAlt, faSignInAlt, faLevelUpAlt, faPlusSquare)

Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.use(VueSweetalert2);


window.axios = require('axios');
window.axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';


Vue.component('toggle-component', require('./components/ToggleComponent').default);
Vue.component('navigation-component', require('./components/NavigationComponent').default);
Vue.component('bot-component', require('./components/BotComponent').default);
Vue.component('login-component', require('./components/LoginComponent').default);
Vue.component('reset-component', require('./components/ResetPasswordComponent').default);
Vue.component('settings-component', require('./components/SettingsComponent').default);

// Instantiate
const app = new Vue({
    el: '#app',
});