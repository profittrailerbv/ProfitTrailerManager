<template>
    <div class="card mb-3">
        <div class="card-body bg-light text-soft-dark">
            <div class="row">
                <div class="col-6">
	                <h5>
	                    <a href="#" @click.prevent="toggleCards()">
	                        <font-awesome-icon v-if="onlyManaged" class="text-success"
	                                           :icon="['fas','toggle-on']"></font-awesome-icon>
	                        <font-awesome-icon v-if="!onlyManaged" class="text-danger"
	                                           :icon="['fas','toggle-off']"></font-awesome-icon>
	                    </a>
	                    <span v-if="onlyManaged">Displaying Only Managed Bots</span>
	                    <span v-if="!onlyManaged">Displaying All Bots</span>
	                </h5>
                </div>
                <div class="col-6 text-right">
	                <h5>
	                    <span v-if="latestVersion != '0.0.0'">Github latest: {{latestVersion}}</span>
	                </h5>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                onlyManaged: false,
                latestVersion: '0.0.0'
            }
        },
        methods: {
            getToggle() {
                axios.get('/api/v1/toggleCards').then((response) => {
                    this.onlyManaged = response.data.onlyManaged
                }).catch((error) => {
                    console.log(error)
                })
            },
            toggleCards() {
                axios.post('/api/v1/toggleCards').then((response) => {
                    this.onlyManaged = response.data.onlyManaged
                }).catch((error) => {
                    console.log(error)
                })
            },
            getToggleData() {
                axios.get('/api/v1/toggleData').then((response) => {
                    this.latestVersion = response.data.latestVersion
                }).catch((error) => {
                    console.log(error)
                })
            },
        },
        mounted() {
            this.getToggle();
            this.getToggleData();
        }
    }
</script>

<style scoped>
</style>