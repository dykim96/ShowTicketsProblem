/*  Assignment 6: Show Tickets Problem
 *  Create ticket services by using multi-threaded ticket clients
 *  and connect them with server using network
 *  Section: 16185
 *  Name: Doyoung Kim
 *  UTEID: dk24338
 *  Name: Connor Lewis
 *  UTEID: csl735
 */
package assignment6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TicketServer {
	static int PORT = 2222;
	// EE422C: no matter how many concurrent requests you get,
	// do not have more than three servers running concurrently
	final static int MAXPARALLELTHREADS = 3;

	public static void start(int portNumber) throws IOException {
		PORT = portNumber;
		Runnable serverThread = new ThreadedTicketServer();
		Thread t = new Thread(serverThread);
		t.start();
	}
}

class ThreadedTicketServer implements Runnable {

	private String hostname;
	private String threadname;
	private String testcase;
	private TicketClient sc;
	//Seat goes from A to Z and 101 to 128
	//
	//A to M are front
	//N to Z are back
	//101 to 107 are left
	//108 to 121 are center
	//122 to 128 are right
	//
	//priority: front center > front side > back center > back side
	private boolean theaterSeats[][];
	private Queue<String> seatQueue;
	private ArrayList<String> boxOffice = new ArrayList<String>();
	
	public ThreadedTicketServer(){
		hostname = "127.0.0.1";
		threadname = "X";
		theaterSeats = new boolean[26][28];
		seatQueue = new LinkedList<String>();
		for(int i = 0; i < theaterSeats.length; i++){
			for(int j = 0; j < theaterSeats[i].length; j++){
				theaterSeats[i][j] = true;
			}
		}
		resetSeatQueue(seatQueue);
	}
	
	//synchronized box office ArrayList adder
	synchronized void addBoxOffice(String booth){
		boxOffice.add(booth);
	}

	//synchronized box office ArrayList remover
	synchronized void removeBoxOffice(String booth){
		boxOffice.remove(booth);
	}
	
	private void resetSeatQueue(Queue<String> seats){
		//add front center
		for(char row = 'A'; row <= 'M'; row++){
			for(int i = 108; i <= 121; i++){
				seats.add(row + " " + i);
			}
		}
		//add front left
		for(char row = 'A'; row <= 'M'; row++){
			for(int i = 101; i <= 107; i++){
				seats.add(row + " " + i);
			}
		}
		//add front right
		for(char row = 'A'; row <= 'M'; row++){
			for(int i = 122; i <= 128; i++){
				seats.add(row + " " + i);
			}
		}
		//add back center
		for(char row = 'N'; row <= 'Z'; row++){
			for(int i = 108; i <= 121; i++){
				seats.add(row + " " + i);
			}
		}
		//add back left
		for(char row = 'N'; row <= 'Z'; row++){
			for(int i = 101; i <= 107; i++){
				seats.add(row + " " + i);
			}
		}
		//add back right
		for(char row = 'N'; row <= 'Z'; row++){
			for(int i = 122; i <= 128; i++){
				seats.add(row + " " + i);
			}
		}
	}

	public void run() {
		// TODO 422C
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(TicketServer.PORT);
			while(true){
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				String user = in.readLine();
				//track different boxOffices
				if(!boxOffice.contains(user))
					addBoxOffice(user);//register new office
				String command = in.readLine();
				//get best available seat
				if(command.equals("bestAvailableSeat")){
					//seat will be -1 if there's none left
					String seat = bestAvailableSeat();
					out.println(seat);
					//all seats are sold, so tell boxOffice that there's no seat
					if (seatQueue.size() == 0){
						removeBoxOffice(user);
						if(boxOffice.size() == 0){
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String bestAvailableSeat(){
		if(!seatQueue.isEmpty())
			return seatQueue.peek();
		return "-1";
	}
}