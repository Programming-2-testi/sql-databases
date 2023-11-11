package databases.part03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import databases.part02.Artist;

public class AlbumDAOTest {
    /**
     * The connection string used to connect to the database. Note that this string
     * is different from the one used in the main() method. This is because the
     * tests use a different database than the main() method.
     */
    private static final String JDBC_URL = "jdbc:sqlite:data/Chinook_Sqlite_TEST.sqlite";

    /**
     * The DAO we are testing. We use a different DAO with a different JDBC_URL for
     * testing because the other database contains data that we don't want to modify
     * or delete.
     */
    private AlbumDAO albumDAO = new AlbumDAO(JDBC_URL);

    private final Artist artist1 = new Artist(1, "AC/DC"); // artist 1 has 2 albums in test database
    private final Artist artist3 = new Artist(3, "Artist 3"); // no albums in test database

    /**
     * Ideally, each test should be independent of the others. This is accomplished
     * by clearing the database before each test and by using a test fixture, so
     * each test runs with the same initial data.
     *
     * @throws SQLException
     */
    @BeforeEach
    void clearDatabase() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL);
        connection.prepareStatement("DELETE FROM Album").executeUpdate();
        connection
                .prepareStatement("""
                        INSERT INTO Album (Title, ArtistId) VALUES
                        ('Album 1 by artist 1', 1),
                        ('Album 2 by artist 1', 1),
                        ('Album 3 by artist 2', 2);
                        """)
                .executeUpdate();
    }

    @Test
    void testGettingAlbumsForArtist() {
        List<Album> albums = albumDAO.getAlbumsByArtist(artist1);

        assertEquals(2, albums.size());
        assertEquals("Album 1 by artist 1", albums.get(0).getTitle());
        assertEquals("Album 2 by artist 1", albums.get(1).getTitle());
    }

    @Test
    void testGettingAlbumsForArtistWithNoAlbums() {
        List<Album> albums = albumDAO.getAlbumsByArtist(artist3);

        assertEquals(0, albums.size());
    }

    @Test
    void testAddingAlbum() {
        Album album = new Album("New Album", 1);

        boolean added = albumDAO.addAlbum(album);
        assertTrue(added);

        List<Album> albums = albumDAO.getAlbumsByArtist(artist1);
        assertEquals(3, albums.size());

        Album latestAlbum = albums.get(albums.size() - 1);
        assertEquals("New Album", latestAlbum.getTitle());
    }

    @Test
    void testAddingAlbumWithSpecialCharacters() {
        // The album's title contains characters that need to be escaped in SQL queries:
        String title = "~~~ The album's \"name\"";

        Album album = new Album(title, 1);

        boolean added = albumDAO.addAlbum(album);
        assertTrue(added);

        List<Album> albums = albumDAO.getAlbumsByArtist(artist1);
        assertEquals(3, albums.size());

        Album latestAlbum = albums.get(albums.size() - 1);
        assertEquals(title, latestAlbum.getTitle());
    }

    @Test
    void testUpdatingAlbums() {
        List<Album> albums = albumDAO.getAlbumsByArtist(artist1);
        Album latestAlbum = albums.get(albums.size() - 1);
        latestAlbum.setTitle("Updated Title");

        boolean updated = albumDAO.updateAlbum(latestAlbum);
        assertTrue(updated);

        List<Album> albumsAfterUpdate = albumDAO.getAlbumsByArtist(artist1);
        Album lastAfterUpdate = albumsAfterUpdate.get(albumsAfterUpdate.size() - 1);
        assertEquals("Updated Title", lastAfterUpdate.getTitle());
    }

    @Test
    void testDeletingAlbums() {
        List<Album> albums = albumDAO.getAlbumsByArtist(artist1);
        assertEquals(2, albums.size());

        Album latestAlbum = albums.get(albums.size() - 1);
        boolean deleted = albumDAO.deleteAlbum(latestAlbum);
        assertTrue(deleted);

        List<Album> albumsAfterDelete = albumDAO.getAlbumsByArtist(artist1);
        assertEquals(1, albumsAfterDelete.size());
    }
}
