component output=false {

	private boolean function task_1( event, rc, prc ) output=false schedule="* 5 * * * *" timeout=120 displayname="Task 1" hint="This is scheduled task 1" {
		return true;
	}

}