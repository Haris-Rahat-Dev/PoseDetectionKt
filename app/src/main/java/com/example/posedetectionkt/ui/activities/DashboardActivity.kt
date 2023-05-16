package com.example.posedetectionkt.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.posedetectionkt.R
import com.example.posedetectionkt.databinding.ActivityDashboardBinding
import com.example.posedetectionkt.ui.fragments.HomeFragment
import com.example.posedetectionkt.utils.WindowManager
import com.example.posedetectionkt.utils.preference.UserDetails
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth;

    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowManager.statusBarManager(
            this,
            R.color.my_light_primary,
            false
        )

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setSupportActionBar(binding.appToolbar.tbToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24)

        // set the profile_name in the navigation drawer header
        val headerView = navView.getHeaderView(0)
        val profileName = headerView.findViewById<TextView>(R.id.profile_name)
        profileName.text = UserDetails(this).getUserEmail()

        // set the home fragment as the default fragment and set it to be checked
        replaceFragment(HomeFragment(), "Home")
        navView.setCheckedItem(R.id.nav_home)

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    binding.drawerLayout.close()
                    replaceFragment(HomeFragment(), it.title.toString())
                }

                R.id.menu_profile -> {
                    binding.drawerLayout.close()
                }

                R.id.nav_explore -> {
                    binding.drawerLayout.close()
                    startActivity(Intent(this@DashboardActivity, ArticlesActivity::class.java))
                }

                R.id.nav_logout -> {
                    auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    // remove the stored credentials from the SharedPreferences object
//                    val sharedPrefs = getSharedPreferences("user", Context.MODE_PRIVATE)
//                    val editor = sharedPrefs.edit()
//                    editor.remove("email")
//                    editor.putString("email", null)
//                    editor.apply()

                    UserDetails(this).clearData()

                    binding.drawerLayout.close()
                    // go to the login activity
                    val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        supportActionBar?.title = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item);
    }
}