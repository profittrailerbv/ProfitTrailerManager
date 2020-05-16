<template>
    <div class="col mb-4">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">{{ botName }}</h5>
                <p>{{ bot }}</p>
                <a :href="'/status/' + botName">{{ bot.status }}</a>
                <p class="card-text">This is a longer card with supporting text below as a natural lead-in to additional content. This content is a little bit longer.</p>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['botName', 'bot']
    }
</script>

<style scoped>

</style>