package com.a2client.model;

import com.a2client.MapCache;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.model.Tile.TILE_ATLAS_SIZE;

/**
 * Created by arksu on 22.02.15.
 */
public class GridChunk
{
    private static final Logger _log = LoggerFactory.getLogger(GridChunk.class.getName());

    public static final int CHUNK_SIZE = 10;
    public static final boolean RANDOM_HEIGHT = true;

    /**
     * меш
     */
    private Mesh _mesh;

    /**
     * массив вершин
     */
    private float[] _vertex;

    /**
     * массив индексов
     */
    private short[] _index;

    private BoundingBox _boundingBox;

    public Mesh getMesh()
    {
        return _mesh;
    }

    public GridChunk(Grid grid, int gx, int gy)
    {
        _vertex = new float[CHUNK_SIZE * CHUNK_SIZE * 9 * 4];
        _index = new short[CHUNK_SIZE * CHUNK_SIZE * 6];
        _mesh = new Mesh(
                true,
                _vertex.length / 3,
                _index.length,
                new VertexAttribute(
                        VertexAttributes.Usage.Position, 3,
                                    ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(
                        VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4,
                                    ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(
                        VertexAttributes.Usage.TextureCoordinates, 2,
                        ShaderProgram.TEXCOORD_ATTRIBUTE)
        );

        makeMesh(grid, gx, gy);
    }

    protected void makeMesh(Grid grid, int gx, int gy)
    {
        int idx = 0;
        int idv = 0;
        int ox = (grid.getGC().x / MapCache.TILE_SIZE);
        int oy = (grid.getGC().y / MapCache.TILE_SIZE);
        _boundingBox = new BoundingBox(new Vector3(ox + gx, oy + gy, -1),
                                       new Vector3(ox + gx + CHUNK_SIZE, oy + gy + CHUNK_SIZE, 3));

        short vertex_count = 0;
        for (int x = gx; x < gx + CHUNK_SIZE; x++)
        {
            for (int y = gy; y < gy + CHUNK_SIZE; y++)
            {
                int tx;
                int ty;
                float f;
                Vector2 uv;

                // 0 =====
                tx = ox + x;
                ty = oy + y;
                int h = tx+ty+x+y;
                _vertex[idx++] = tx;
                _vertex[idx++] = ty;
                f = -(h % 10) / 40f;
                _vertex[idx++] = f;
                idx += 4;
                uv = Tile.getTileUV(grid._tiles[y][x]);
                _vertex[idx++] = uv.x;
                _vertex[idx++] = uv.y;
                // 1 =====
                _vertex[idx++] = tx + 1;
                _vertex[idx++] = ty;
                _vertex[idx++] = f;
                idx += 4;
                _vertex[idx++] = uv.x + TILE_ATLAS_SIZE;
                _vertex[idx++] = uv.y;
                // 2 =====
                _vertex[idx++] = tx;
                _vertex[idx++] = ty + 1;
                _vertex[idx++] = f;
                idx += 4;
                _vertex[idx++] = uv.x;
                _vertex[idx++] = uv.y + TILE_ATLAS_SIZE;
                // 3 =====
                _vertex[idx++] = tx + 1;
                _vertex[idx++] = ty + 1;
                _vertex[idx++] = f;
                idx += 4;
                _vertex[idx++] = uv.x + TILE_ATLAS_SIZE;
                _vertex[idx++] = uv.y + TILE_ATLAS_SIZE;

                _index[idv++] = (vertex_count);
                _index[idv++] = (short) (vertex_count + 1);
                _index[idv++] = (short) (vertex_count + 2);
                _index[idv++] = (short) (vertex_count + 2);
                _index[idv++] = (short) (vertex_count + 1);
                _index[idv++] = (short) (vertex_count + 3);

                vertex_count += 4;

            }
        }
        _mesh.setVertices(_vertex);
        _mesh.setIndices(_index);
    }

    public BoundingBox getBoundingBox()
    {
        return _boundingBox;
    }
}
