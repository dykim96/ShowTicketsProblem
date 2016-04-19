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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

class ThreadedTicketClient implements Runnable {
	private String hostname = "127.0.0.1";
	private String threadname = "X";
	private TicketClient sc;
	private boolean soldOut;
	private String seat;
	private boolean reservedASeat;

	public ThreadedTicketClient(TicketClient sc, String hostname, String threadname) {
		this.sc = sc;
		this.hostname = hostname;
		this.threadname = threadname;
		soldOut = false;
		seat = "";
		reservedASeat = false;
	}

	public void run() {
		System.out.flush();
		try {
			//run until successfully reserving a seat or until there's no more seat
			while(!reservedASeat && !soldOut){
				Socket echoSocket = new Socket(hostname, TicketServer.PORT);
				PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				out.println(threadname);
				//get best available seat
				if(seat.isEmpty()){
					out.println("bestAvailableSeat");
					String answer = in.readLine();
					//when first answer is -1, this means there's no seat left
					if(answer.equals("-1")){
						soldOut = true;
						System.out.println("Box Office " + threadname + ": sorry, we are sold out");
					}
					//since there is/are seat(s) left, continue
					else{
						soldOut = false;
						seat = answer;
						System.out.println("Box Office " + threadname + ": Best available seat is " + seat);
					}
				}
				//print the ticket
				else if(seat.substring(0, 5).equals("print")){
					out.println(seat);
					seat = "";
					reservedASeat = true;
				}
				//reserve the seat
				else{
					out.println(seat);
					String answer = in.readLine();
					if(answer.equals("success")){
						System.out.println("Box Office " + threadname + ": Reserved HR, " + seat);
						seat = "print " + seat;
					}
					else{
						System.out.println("Box Office " + threadname + ": Failed to reserve HR, " + seat);
						seat = "";
					}
				}
				echoSocket.close();
			}
			reservedASeat = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	boolean isSoldOut(){
		return soldOut;
	}
}

public class TicketClient {
	private ThreadedTicketClient tc;
	private String hostName = "";
	private String threadName = "";
	private int lineLength = 0;
	private Random rand;

	TicketClient(String hostname, String threadname) {
		tc = new ThreadedTicketClient(this, hostname, threadname);
		hostName = hostname;
		threadName = threadname;
		rand = new Random();
		lineLength = rand.nextInt(901) + 100;
	}

	TicketClient(String name) {
		this("localhost", name);
	}

	TicketClient() {
		this("localhost", "unnamed client");
	}

	boolean requestTicket() {
		// TODO thread.run()
		if(lineLength <= 0){
			lineLength = rand.nextInt(901) + 100;
		}
		tc.run();
		//System.out.println(hostName + "," + threadName + " got one ticket");
		return tc.isSoldOut();
	}

	void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
