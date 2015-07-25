package board;

public class Piece {
	
	/*********************   Variablen   ************************/
	
	private Position position;
    private int color;
    private int type;
    
    /*********************   Konstruktoren   ************************/
    
    public Piece () {
    	position = new Position ();
    	color = Constants.Color.NONE;
    	type = Constants.Type.NONE;
    }
    
    public Piece (int color, int type) {
    	this.color = color;
    	this.type = type;
    }
    
    public Piece (Position position) {
    	this.position = position;
    }

    public Piece (Position position, int color, int type) {
		this.position = position;
		this.color = color;
		this.type = type;
	}

    /*********************   Getter / Setter   ************************/
    
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public char getPrefix() {
		char prefix = 'X';
		switch (this.type) {
		case Constants.Type.ELEPHANT:
			prefix = (color == Constants.Color.GOLD) ? 'E'
					: 'e';
			break;
		case Constants.Type.CAMEL:
			prefix = (color == Constants.Color.GOLD) ? 'M'
					: 'm';
			break;
		case Constants.Type.HORSE:
			prefix = (color == Constants.Color.GOLD) ? 'H'
					: 'h';
			break;
		case Constants.Type.DOG:
			prefix = (color == Constants.Color.GOLD) ? 'D'
					: 'd';
			break;
		case Constants.Type.CAT:
			prefix = (color == Constants.Color.GOLD) ? 'C'
					: 'c';
			break;
		case Constants.Type.RABBIT:
			prefix = (color == Constants.Color.GOLD) ? 'R'
					: 'r';
			break;
		}
		return prefix;
	}
}
