package ca.dawsoncollege.project_pokemon

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// db
@Database(entities = [PlayerTrainer::class], version = 1)
@TypeConverters(DataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Dao
interface UserDao {
    // save trainer to db
    @Insert
    fun savePlayerTrainer(player: PlayerTrainer)

    // fetch trainer
    @Query("SELECT * FROM PlayerTrainer")
    fun fetchPlayerSave(): PlayerTrainer?

    // clear database
    @Query("DELETE FROM PlayerTrainer")
    fun delete()

    // Update methods for team and collection
    @Query("UPDATE PlayerTrainer SET team = :updatedTeam")
    fun updateTeam(updatedTeam: ArrayList<Pokemon>)

    @Query("UPDATE PlayerTrainer SET pokemon_collection = :updatedTeam")
    fun updateCollection(updatedTeam: ArrayList<Pokemon>)

}

// type convert to convert pok  emon arraylist to json and vice versa
class DataConverter {
    @TypeConverter
    fun pokeListToJson(pokeList: ArrayList<Pokemon>): String {
        val gson = Gson();
        val type = object : TypeToken<ArrayList<Pokemon>>() {}.type
        return gson.toJson(pokeList, type)
    }

    @TypeConverter
    fun jsonToPokeList(jsonString: String): ArrayList<Pokemon> {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Pokemon>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}