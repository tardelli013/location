package br.com.tardelli.location.google.places;

class PlacesException extends RuntimeException {

    private static final long serialVersionUID = -7551318600432160726L;

    PlacesException(Throwable t) {
        super(t);
    }
}
