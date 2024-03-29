package ca.dawsoncollege.project_pokemon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwitchPokemonFragment : Fragment() {
    private lateinit var battle: Battle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = arguments
        val battleJSON = data!!.getString("battle").toString()
        val battleType = data.getString("type").toString()
        if (battleType == "wild")
            this.battle = convertJSONToWildBattle(battleJSON)
        else
            this.battle = convertJSONToTrainerBattle(battleJSON)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_switch_pokemon, container, false).rootView
        val buttons = arrayListOf<Button>(
            view.findViewById(R.id.pokemon_1),
            view.findViewById(R.id.pokemon_2),
            view.findViewById(R.id.pokemon_3),
            view.findViewById(R.id.pokemon_4),
            view.findViewById(R.id.pokemon_5),
            view.findViewById(R.id.pokemon_6))
        setPokemons(buttons)
        return view
    }

    // set text and listener to button corresponding to pokemon in team
    private fun setPokemons(buttons: ArrayList<Button>){
        for (i in 0 until this.battle.playerTrainer.team.size){
            val pokemonButtonText = "${this.battle.playerTrainer.team[i].name}\n"+
                    "LV ${this.battle.playerTrainer.team[i].level}\n"+
                    "${this.battle.playerTrainer.team[i].hp}/${this.battle.playerTrainer.team[i].battleStat.maxHP}"
            // set text
            buttons[i].text = pokemonButtonText
            // set listener
            buttons[i].setOnClickListener {
                // switch current pokemon with ith pokemon
                try{
                    val listener = activity as Callbacks
                    this.battle.switchSelectPlayerPkm(this.battle.playerTrainer.team[i], i)
                    listener.updateBattleText("You switched to ${this.battle.playerPokemon.name}!")
                    lifecycleScope.launch(Dispatchers.Main){
                        this@SwitchPokemonFragment.battle = performEnemyMove(this@SwitchPokemonFragment.battle, listener)
                        listener.updatePokemonUI(this@SwitchPokemonFragment.battle)
                        replaceWithMovesFragment()
                    }
                } catch (e: Battle.SamePokemonException){
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                catch (e: IllegalArgumentException){
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // start moves fragment and replace current fragment (switch pokemon fragment)
    private fun replaceWithMovesFragment(){
        val battleActivity = activity as BattleActivity
        val movesFragment = MovesFragment()
        val bundle = Bundle()
        bundle.putString("battle", convertBattleToJSON(this.battle))
        movesFragment.arguments = bundle
        battleActivity.supportFragmentManager.beginTransaction().apply {
            replace(R.id.battle_menu_fragment, movesFragment)
            commit()
        }
    }
}