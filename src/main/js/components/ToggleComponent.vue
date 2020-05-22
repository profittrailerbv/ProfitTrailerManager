<template>
    <div class="row row-cols-1 row-cols-md-1">
        <div class="card">
            <div class="card-body" v-if="onlyManaged">
                <h5>
                <a href="#" @click.prevent="toggleCards()">
                    <i class="fas fa-toggle-on text-success"></i>
                </a>
                Toggle to display only managed bots
                </h5>
            </div>
            <div class="card-body" v-if="!onlyManaged">
                <h5>
                <a href="#" @click.prevent="toggleCards()">
                    <i class="fas fa-toggle-off text-danger"></i>
                </a>
                Toggle to display only managed bots
                </h5>
            </div>
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
                    console.log(response.data)
                    this.onlyManaged = response.data.onlyManaged
                    console.log("After get " + this.onlyManaged)
                }).catch((error) => {
                    console.log(error)
                })
            },
            toggleCards() {
                axios.post('/api/v1/toggleCards').then((response) => {
                    console.log(response.data)
                    this.onlyManaged = response.data.onlyManaged
                }).catch((error) => {
                    console.log(error)
                })
            }
        },
        mounted() {
            this.getToggle()
        },
        computed: {
       }
    }
</script>

<style scoped>

</style>