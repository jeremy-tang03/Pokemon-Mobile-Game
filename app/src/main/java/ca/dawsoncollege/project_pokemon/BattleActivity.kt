package ca.dawsoncollege.project_pokemon

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import ca.dawsoncollege.project_pokemon.databinding.ActivityBattleBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class BattleActivity : AppCompatActivity(), Callbacks {
    private lateinit var binding: ActivityBattleBinding
    private lateinit var battle: Battle
    private lateinit var battleType: String
    private lateinit var playerTrainer: PlayerTrainer
    private lateinit var enemyTrainer: EnemyTrainer
    private lateinit var userDao: UserDao
    private val battleTextList = arrayListOf("", "", "")
    private var userPick = 10

    companion object {
        private const val LOG_TAG = "BATTLE_ACTIVITY_DEV_LOG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle: Bundle? = intent.extras
        battleType = bundle!!.getString("type").toString()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "Trainer-Database"
        ).build()

        this.userDao = db.userDao()

        lifecycleScope.launch(Dispatchers.IO) {
            if (this@BattleActivity.userDao.fetchPlayerSave() != null) {
                playerTrainer = this@BattleActivity.userDao.fetchPlayerSave()!!
                if (battleType == "wild")
                    this@BattleActivity.battle = WildBattle(playerTrainer)
                else{
                    this@BattleActivity.enemyTrainer = EnemyTrainer()
                    this@BattleActivity.battle = TrainerBattle(playerTrainer, this@BattleActivity.enemyTrainer)
                }
                withContext(Dispatchers.Main){
                    setPlayerPokemonUI()
                    setEnemyPokemonUI()
                    setMovesFragment()
                }
            }
        }
        binding.movesBtn.setOnClickListener {
            setMovesFragment()
        }
        binding.switchBtn.setOnClickListener {
            setSwitchPokemonFragment()
        }
        binding.itemsBtn.setOnClickListener {
            setItemsFragment()
        }
        binding.runBtn.setOnClickListener {
            if (battleType == "wild"){
                this.battle.updatePlayerPokemon()
                this.battle = this.battle.playerRun()
                this.playerTrainer = this.battle.playerTrainer
                runBlocking(Dispatchers.IO) {
                    if (userDao.fetchPlayerSave() != null) userDao.delete()
                    userDao.savePlayerTrainer(this@BattleActivity.playerTrainer)
                }
                finish()
            } else
                Toast.makeText(applicationContext, R.string.trainer_battle_run, Toast.LENGTH_SHORT).show()
        }
        showButtons()
    }

    // set entire player pokemon UI
    private fun setPlayerPokemonUI() {
        binding.playerPokemonName.text = this.battle.playerPokemon.name.toString()
        lifecycleScope.launch(Dispatchers.IO) {
            val backSprite = BitmapFactory.decodeStream(
                URL(this@BattleActivity.battle.playerPokemon.data.backSprite).openConnection()
                    .getInputStream()
            )
            withContext(Dispatchers.Main) {
                binding.playerPokemonSprite.setImageBitmap(backSprite)
            }
        }
        val pokemonLevelText = "LV " + this.battle.playerPokemon.level.toString()
        binding.playerPokemonLevel.text = pokemonLevelText
        updateHP(this.battle.playerPokemon, true)
    }

    // set entire enemy pokemon UI
    private fun setEnemyPokemonUI() {
        binding.enemyPokemonName.text = this.battle.enemyPokemon.name.toString()
        lifecycleScope.launch(Dispatchers.IO) {
            val frontSprite = BitmapFactory.decodeStream(
                URL(this@BattleActivity.battle.enemyPokemon.data.frontSprite).openConnection()
                    .getInputStream()
            )
            withContext(Dispatchers.Main) {
                binding.enemyPokemonSprite.setImageBitmap(frontSprite)
            }
        }
        val pokemonLevelText = "LV " + this.battle.enemyPokemon.level.toString()
        binding.enemyPokemonLevel.text = pokemonLevelText
        updateHP(this.battle.enemyPokemon, false)
    }

    // update HP UI of given pokemon
    private fun updateHP(pokemon: Pokemon, isPlayer: Boolean) {
        val hp = pokemon.hp.toString() + "/" + pokemon.battleStat.maxHP.toString()
        if (isPlayer) {
            binding.playerPokemonHealth.text = hp
        } else {
            binding.enemyPokemonHealth.text = hp
        }
    }

    // start moves fragment
    private fun setMovesFragment() {
        val movesFragment = MovesFragment()
        val bundle = Bundle()
        bundle.putString("battle", convertBattleToJSON(this.battle))
        bundle.putString("type", battleType)
        movesFragment.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.battle_menu_fragment, movesFragment)
            commit()
        }
    }

    // start switch pokemon fragment
    private fun setSwitchPokemonFragment() {
        val switchPokemonFragment = SwitchPokemonFragment()
        val bundle = Bundle()
        bundle.putString("battle", convertBattleToJSON(this.battle))
        bundle.putString("type", battleType)
        switchPokemonFragment.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.battle_menu_fragment, switchPokemonFragment)
            commit()
        }
    }

    // start items fragment
    private fun setItemsFragment() {
        val itemsFragment = ItemsFragment()
        val bundle = Bundle()
        bundle.putString("battle", convertBattleToJSON(this.battle))
        bundle.putString("type", battleType)
        itemsFragment.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.battle_menu_fragment, itemsFragment)
            commit()
        }
    }

    // callback for fragments to update battle data in this activity
    @Override
    override fun updateTeam(battle: Battle) {
        this.battle = battle
    }

    // callback for fragments to update battle data and HP UI in this activity
    @Override
    override fun updateHPUI(battle: Battle) {
        this.battle = battle
        this.playerTrainer = battle.playerTrainer
        updateHP(this.battle.playerPokemon, true)
        updateHP(this.battle.enemyPokemon, false)
    }

    // callback for fragments to update battle data and entire battle UI in this activity
    @Override
    override fun updatePokemonUI(battle: Battle) {
        this.battle = battle
        setPlayerPokemonUI()
        setEnemyPokemonUI()
    }

    // callback for fragments to update battle text view
    @Override
    override fun updateBattleText(message: String) {
        battleTextList.removeLast()
        battleTextList.add(0, message)
        val text = battleTextList[2]+"\n"+battleTextList[1]+"\n"+battleTextList[0]
        binding.battleText.text = text;
    }

    @Override
    override fun reloadMovesFragment(battle: Battle) {
        this.battle = battle
        this.setMovesFragment()
    }

    @Override
    override fun triggerPlayTurn(battle: Battle, moveList: ArrayList<Move>, buttons: ArrayList<Button>, i: Int) {
        this.battle = battle
        // specific dispatcher is specified in the functions involved
        lifecycleScope.launch(Dispatchers.Main){
            playTurn(moveList, buttons, i)
            Log.d("EXTENSION", "enemy: "+battle.enemyPokemon.hp.toString())
            Log.d("EXTENSION", "player: "+battle.playerPokemon.hp.toString())
            // callback to update HP UI in BattleActivity
            updateHPUI(battle)
        }
    }

    // update button text
    private fun updateMovePP(button: Button, move: Move) {
        val moveButtonText = "${move.name.replace('-', ' ')}\n" +
                "${move.PP}/${move.maxPP}\n${move.type}"
        button.text = moveButtonText
    }

    // play a turn
    private suspend fun playTurn(moveList: ArrayList<Move>, buttons: ArrayList<Button>, i: Int){
        // check who attacks first
        val listener = this as Callbacks
        if (this.battle.playerPokemon.battleStat.speed >= this.battle.enemyPokemon.battleStat.speed){
            if (!this.battle.checkPokemonFainted()){
                val oldEnemy = this.battle.enemyPokemon
                // attempt move, set text, update pp and button
                performPlayerMove(moveList, buttons, i, listener)
                if (oldEnemy == this.battle.enemyPokemon)
                    this.battle = performEnemyMove(this.battle, listener)
            } else { //TODO: might not need this after end the battle properly
                this.battle.enemyPokemon.name?.let { name -> listener.updateBattleText(name + " " + getString(R.string.fainted)) }
            }
        } else {
            this.battle = performEnemyMove(this.battle, listener)
            // if player pokemon is not fainted
            performPlayerMove(moveList, buttons, i, listener)
        }
        // update player data
        this.battle.updatePlayerPokemon()
    }

    private suspend fun proposeMovePrompt(){
        println("proposing")
        var madeChoice = false
        val newMoves = this.battle.playerPokemon.proposeMove()
        for (mov in newMoves)
            println(mov.name)
        println("here")
        hideButtons()
        println("hid buttons")
        for (move in newMoves){
            println("in loop new moves")
            updateBattleText("${this.battle.playerPokemon.name} wants to learn ${move.name.replace('-', ' ')}")
            val learnMoveFragment = LearnMoveFragment()
            val bundle = Bundle()
            bundle.putString("battle", convertBattleToJSON(this.battle))
            bundle.putString("type", battleType)
            learnMoveFragment.arguments = bundle
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.battle_menu_fragment, learnMoveFragment)
                commit()
            }
            while (!madeChoice){
                println("in loop")
                // wait for callback
                if(this.userPick < 5){
                    madeChoice = true
                    this.battle.playerPokemon.learnMove(move,
                        this.battle.playerPokemon.moveList[this.userPick])
                } else if (this.userPick == 9){
                    madeChoice = true
                }
            }
            this.battle.updatePlayerPokemon()
            this.userPick = 10
            madeChoice = false
        }
        setMovesFragment()
        showButtons()
    }

    private suspend fun performPlayerMove(moveList: ArrayList<Move>, buttons: ArrayList<Button>, i: Int, listener: Callbacks){
        // if player pokemon is not fainted
        if (this.battle.playerPokemon.hp != 0){
            if (!this.battle.checkPokemonFainted()){
                playPlayerMove(moveList[i], buttons[i])
            }
            val oldLevel = this.battle.playerPokemon.level
            if (this.battle.checkPokemonFainted()){
                // Toast.makeText(context, "${this.battle.enemyPokemon.name} fainted!", Toast.LENGTH_SHORT).show()
                this.battle.enemyPokemon.name?.let { name -> listener.updateBattleText(name + " " + getString(R.string.fainted)) }
                if (this.battle.playerPokemon.level > oldLevel){
                    this.battle.updatePlayerPokemon()
                    listener.updatePokemonUI(this.battle)
                    this.battle.playerPokemon.name?.let { name -> listener.updateBattleText(name + " " + getString(R.string.level_up)) }
//                    println("before prompt")
//                    proposeMovePrompt()
//                    println("after prompt")
                }
                if (battleType == "wild"){
                    winBattle(this.battle)
                } else {
                    if ((this.battle as TrainerBattle).switchOutEnemyPkm()){
                        listener.updatePokemonUI(this.battle)
                    } else
                        winBattle(this.battle)
                }
            }
        } else {
//            Toast.makeText(context, "${this.battle.playerPokemon.name} fainted!", Toast.LENGTH_SHORT).show()
            this.battle.playerPokemon.name?.let { name -> listener.updateBattleText(name + " " + getString(R.string.fainted)) }
            if(this.battle.switchOutPlayerPkm()){
                listener.updatePokemonUI(this.battle)
                listener.reloadMovesFragment(this.battle)
            }
            else
                listener.loseBattle(this.battle)
        }
    }

    // helper method, plays move and sets battle text for it
    private suspend fun playPlayerMove(move: Move, button: Button) {
        val listener = this as Callbacks
        if(this.battle.playerMove(move)) {
            this.battle.playerPokemon.name?.let { listener.updateBattleText(it + " " +
                    getString(R.string.used) + " " + move.name.replace('-', ' ')) }
        }
        // missed move
        else {
            this.battle.playerPokemon.name?.let { listener.updateBattleText(it + " " + getString(R.string.miss_move))}
        }
//        Log.d("MOVES_FRAG", move.toString())
        move.PP -= 1
        updateMovePP(button, move)
    }

    @Override
    override fun onBackPressed() {
        // super.onBackPressed();
    }

    fun hideButtons(){
        binding.movesBtn.visibility = View.INVISIBLE
        binding.switchBtn.visibility = View.INVISIBLE
        binding.itemsBtn.visibility = View.INVISIBLE
        binding.runBtn.visibility = View.INVISIBLE
    }

    fun showButtons(){
        binding.movesBtn.visibility = View.VISIBLE
        binding.switchBtn.visibility = View.VISIBLE
        binding.itemsBtn.visibility = View.VISIBLE
        binding.runBtn.visibility = View.VISIBLE
    }

    private fun winBattle(battle: Battle) {
        this.battle = battle
        supportFragmentManager.beginTransaction().apply {
            remove(supportFragmentManager.fragments[0])
            commit()
        }
        hideButtons()
        Toast.makeText(applicationContext, "You won a $battleType battle!", Toast.LENGTH_LONG).show()
        this.battle.updatePlayerPokemon()
        this.playerTrainer = this.battle.playerTrainer
        lifecycleScope.launch(Dispatchers.IO) {
            if (userDao.fetchPlayerSave() != null) userDao.delete()
            userDao.savePlayerTrainer(this@BattleActivity.playerTrainer)
            withContext(Dispatchers.Main){
                Timer().schedule(2000) {
                    finish()
                }
            }
        }
    }

    @Override
    override fun loseBattle(battle: Battle) {
        this.battle = battle
        supportFragmentManager.beginTransaction().apply {
            remove(supportFragmentManager.fragments[0])
            commit()
        }
        hideButtons()
        Toast.makeText(applicationContext, "You lost a $battleType battle...", Toast.LENGTH_LONG).show()
        this.battle.updatePlayerPokemon()
        this.playerTrainer = this.battle.playerTrainer
        lifecycleScope.launch(Dispatchers.IO) {
            if (userDao.fetchPlayerSave() != null) userDao.delete()
            userDao.savePlayerTrainer(this@BattleActivity.playerTrainer)
            withContext(Dispatchers.Main){
                Timer().schedule(2000) {
                    finish()
                }
            }
        }
    }

    @Override
    override fun capturedPokemon(battle: Battle) {
        this.battle = battle
        supportFragmentManager.beginTransaction().apply {
            remove(supportFragmentManager.fragments[0])
            commit()
        }
        hideButtons()
        Toast.makeText(applicationContext, R.string.capture_toast, Toast.LENGTH_LONG).show()
        this.playerTrainer = this.battle.playerTrainer
        lifecycleScope.launch(Dispatchers.IO) {
            if (userDao.fetchPlayerSave() != null) userDao.delete()
            userDao.savePlayerTrainer(this@BattleActivity.playerTrainer)
            withContext(Dispatchers.Main){
                Timer().schedule(3000) {
                    finish()
                }
            }
        }
    }

    @Override
    override fun learnMove(moveIndex: Int) {
        this.userPick = moveIndex
    }
}

// interface containing callbacks
interface Callbacks {
    fun updateTeam(battle: Battle)
    fun updateHPUI(battle: Battle)
    fun updatePokemonUI(battle: Battle)
    fun updateBattleText(message: String)
    fun reloadMovesFragment(battle: Battle)
    fun triggerPlayTurn(battle: Battle, moveList: ArrayList<Move>, buttons: ArrayList<Button>, i: Int)
//    fun winBattle(battle: Battle)
    fun loseBattle(battle: Battle)
    fun capturedPokemon(battle: Battle)
    fun learnMove(moveIndex: Int)
}

// extension functions
// converts Battle object into a JSON string
fun convertBattleToJSON(battle: Battle): String = Gson().toJson(battle)
// converts JSON string back into a Wild Battle object
fun convertJSONToWildBattle(json: String) =
    Gson().fromJson(json, object: TypeToken<WildBattle>(){}.type) as WildBattle
// converts JSON string back into a Trainer Battle object
fun convertJSONToTrainerBattle(json: String) =
    Gson().fromJson(json, object: TypeToken<TrainerBattle>(){}.type) as TrainerBattle

suspend fun performEnemyMove(battle: Battle, listener: Callbacks): Battle{
    // if enemy pokemon is not fainted
    if (!battle.checkPokemonFainted()){
        // move success
        val moveName = battle.playEnemyMove()
        // move succeed
        if (moveName != null) {
            battle.enemyPokemon.name?.let { listener.updateBattleText("$it used ${moveName.replace('-', ' ')}!")}//+ Resources.getSystem().getString(R.string.used) + " " + moveName) }
        }
        // move missed
        else {
            battle.enemyPokemon.name?.let { listener.updateBattleText("$it missed their move!")}// + Resources.getSystem().getString(R.string.miss_move)) }
        }
        if (battle.playerPokemon.hp == 0){
            battle.playerPokemon.name?.let { name -> listener.updateBattleText("$name is fainted!")}//+ Resources.getSystem().getString(R.string.fainted)) }
            if (battle.switchOutPlayerPkm()){
                listener.updatePokemonUI(battle)
                listener.reloadMovesFragment(battle)
            } else {
                listener.loseBattle(battle)
            }
        }
    }
    return battle
}