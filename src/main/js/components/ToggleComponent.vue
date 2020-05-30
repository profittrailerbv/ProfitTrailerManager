<template>
    <div class="card mb-3">
        <div class="card-body bg-light text-soft-dark">
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
    </div>
</template>

<script>
    export default {
        data() {
            return {
                onlyManaged: false
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
            }
        },
        mounted() {
            this.getToggle()
        }
    }
</script>

<style scoped>
</style>