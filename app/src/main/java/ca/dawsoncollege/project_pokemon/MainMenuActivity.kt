package ca.dawsoncollege.project_pokemon

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import ca.dawsoncollege.project_pokemon.databinding.MainMenuBinding

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: MainMenuBinding
    companion object {
        private const val LOG_TAG = "MAIN_MENU_ACTIVITY_DEV_LOG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: replace commented code when fragments are ready
        /*
        // initialize needed fragments
        val defaultFragment = MainMenuActivity() // should be a fragment here instead
        val pokecenterFragment = PokecenterFragment()
        val changeTeamFragment = ChangeTeamFragment()
        val tBattleFragment = TrainerBattleFragment()
        val wBattleFragment = WildBattleFragment()

        // fragment to appear by default
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout3, defaultFragment)
            commit()
        }
        */
        setButtonListeners()
    }

    private fun setButtonListeners(){
        binding.pokecenterBtn.setOnClickListener {
//            supportFragmentManager.beginTransaction().apply {
//                replace(R.id.frameLayout3, pokecenterFragment)
//                commit()
//            }
            Toast.makeText(applicationContext, "pokecenter", Toast.LENGTH_SHORT).show()
        }
        binding.changeTeamBtn.setOnClickListener {
//            supportFragmentManager.beginTransaction().apply {
//                replace(R.id.frameLayout3, changeTeamFragment)
//                commit()
//            }
            Toast.makeText(applicationContext, "change team", Toast.LENGTH_SHORT).show()
        }
        binding.trainerBattleBtn.setOnClickListener {
//            supportFragmentManager.beginTransaction().apply {
//                replace(R.id.frameLayout3, tBattleFragment)
//                commit()
//            }
            Toast.makeText(applicationContext, "pokecenter", Toast.LENGTH_SHORT).show()
        }
        binding.wildBattleBtn.setOnClickListener {
//            supportFragmentManager.beginTransaction().apply {
//                replace(R.id.frameLayout3, wBattleFragment)
//                commit()
//            }
            Toast.makeText(applicationContext, "change team", Toast.LENGTH_SHORT).show()
        }
    }
}