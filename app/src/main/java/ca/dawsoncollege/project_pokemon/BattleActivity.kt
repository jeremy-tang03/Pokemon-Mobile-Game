package ca.dawsoncollege.project_pokemon

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import ca.dawsoncollege.project_pokemon.databinding.ActivityBattleBinding

class BattleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBattleBinding
    private lateinit var playerTrainer: PlayerTrainer
    companion object {
        private const val LOG_TAG = "BATTLE_ACTIVITY_DEV_LOG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreference = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val playerTrainerJson = sharedPreference.getString("playerTrainer", "empty")
        if (playerTrainerJson != "empty") {
            this.playerTrainer = convertToPlayerTrainer(playerTrainerJson!!)
            setPlayerPokemon()
        }
    }

    private fun setPlayerPokemon(){
        // TODO: set imageview to sprite of pokemon using bitmap
        if (this.playerTrainer.team[0].name.isNullOrEmpty()){
            binding.playerPokemonName.text = this.playerTrainer.team[0].species.toString()
        } else {
            binding.playerPokemonName.text = this.playerTrainer.team[0].name.toString()
        }
        binding.playerPokemonLevel.text = this.playerTrainer.team[0].level.toString()
        // TODO: find a way to store max hp value
        val hp = this.playerTrainer.team[0].hp.toString() + "/" + this.playerTrainer.team[0].hp.toString()
        binding.playerPokemonHealth.text = hp
    }
}