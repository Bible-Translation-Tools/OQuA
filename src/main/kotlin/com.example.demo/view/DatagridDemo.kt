package com.example.demo.view
import com.example.demo.controller.DataService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*;

class DatagridDemo: View("Datagrid Demo") {

    override fun onCreate() {
        setWindowMinSize(800, 200);
        println("goodnight goodnight")
        super.onCreate()
    }

    //grab the usernames from outside
    //in the real thing, we'll grab icons and sound clips instead
    private val data: DataService by inject();

    //the left half of the screen, which displays:
    //the last user to log in, a welcome message, and a button to go to that user's home
    val rad = 100.0;
    private val bigIcons = borderpane () {
        //make a big user icon
        val myBigUserIcon = UserIconWidget(rad);
        //set its alignment to center it
        //alignment must be set on root, not on Widget itself
        myBigUserIcon.root.alignment = Pos.CENTER;
        val myHomeWidget = HomeWidget();
        myHomeWidget.root.alignment = Pos.CENTER;

        top = myBigUserIcon.root;
        center = label("Welcome!");
        bottom = myHomeWidget.root;

        setMaxSize(2 * rad + 100, 3 * rad);
        setPrefSize(2 * rad + 100, 3 * rad);
        usePrefSize;
    }

    //the grid of users
    //hooked up to the list we pulled in up top from DataService
    //right now it has just 9 elems but the real one will have who-knows-how-many
    val gridWidth = 600.0;
    private val myGrid = datagrid(data.numbers()) {
        //formats each cell; if not called, cells are just empty white squares
        //the "it" inside is an item from data.numbers
        cellFormat {
            //each cell is a borderpane
            graphic = borderpane() {
                //make a small icon
                val currentSmallUserIcon = UserIconWidget(25.0);
                //set its alignment to center so it shows up in the middle of the cell
                //(otherwise shows up in left)
                currentSmallUserIcon.root.alignment = Pos.CENTER;
                //set it to an area of the borderpane
                top = currentSmallUserIcon.root;

                center = label("user " + it);

                val mydgBottomWidget = HomeWidget();
                mydgBottomWidget.root.alignment = Pos.CENTER;
                bottom = mydgBottomWidget.root;
            }
        }
    }


    private val plusButton = PlusWidget();


    val pad = 60.0;
    private val welcomeScreen = borderpane() {
        //BorderPane.setAlignment(bigIcons, Pos.CENTER)
        //put the elems in the welcomeScreen
        left = stackpane{
            add(bigIcons);
        };
        center = myGrid;
        //make sure the plus button is in the bottom right
        BorderPane.setAlignment(plusButton.root, Pos.BOTTOM_RIGHT);
//        plusButton.root.alignment = Pos.BOTTOM_RIGHT;
        bottom = plusButton.root;
        //put in some nice margins so it's not too crowded
        padding = insets(pad);
    }

    //set the root of the view to the welcomeScreen
    override val root = welcomeScreen;

    //current window will be null unless init goes under setting of root
    init{
        val minWidth = 2 * pad + 2 * rad + gridWidth;
        //add 100 for home button and Welcome message; probably in real thing these will be vars
        val minHeight = 2 * pad + 2 * rad + 100.0;
        setWindowMinSize(minWidth, minHeight);
    }
}

