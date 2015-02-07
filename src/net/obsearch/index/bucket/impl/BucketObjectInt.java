/*     */ package net.obsearch.index.bucket.impl;
/*     */ 
/*     */ import java.nio.ByteBuffer;
/*     */ import java.util.Arrays;
/*     */ import net.obsearch.exception.OBException;
/*     */ import net.obsearch.index.bucket.BucketObject;
/*     */ import net.obsearch.ob.OBInt;
/*     */ 
/*     */ public class BucketObjectInt<O extends OBInt> extends BucketObject<O>
/*     */   implements Comparable<BucketObjectInt>
/*     */ {
/*     */   private int[] smapVector;
/*     */ 
/*     */   public BucketObjectInt()
/*     */   {
/*  47 */     super(-1L);
/*  48 */     this.smapVector = null;
/*     */   }
/*     */ 
/*     */   public BucketObjectInt(int[] smapVector, long id)
/*     */   {
/*  67 */     this(smapVector, id, null);
/*     */   }
/*     */ 
/*     */   public BucketObjectInt(int[] smapVector, long id, O object)
/*     */   {
/*  86 */     super(id, object);
/*  87 */     this.smapVector = smapVector;
/*     */   }
/*     */ 
/*     */   public boolean equals(Object other) {
/*  91 */     BucketObjectInt another = (BucketObjectInt)other;
/*  92 */     boolean pivotVectorsEqual = comparePivotVectors(this.smapVector, another.smapVector);
/*  93 */     boolean idsEqual = getId() == another.getId();
/*  94 */     boolean objectsEqual = ((OBInt)getObject()).equals(another.getObject());
/*  95 */     return ((pivotVectorsEqual) && (idsEqual) && (objectsEqual));
/*     */   }
/*     */ 
/*     */   private boolean comparePivotVectors(int[] a, int[] b) {
/*  99 */     int la = 0;
/* 100 */     int lb = 0;
/* 101 */     if (a != null) {
/* 102 */       la = a.length;
/*     */     }
/* 104 */     if (b != null) {
/* 105 */       lb = b.length;
/*     */     }
/* 107 */     if ((la == 0) && (lb == 0)) {
/* 108 */       return true;
/*     */     }
/*     */ 
/* 111 */     return Arrays.equals(a, b);
/*     */   }
/*     */ 
/*     */   public int lInf(BucketObjectInt b)
/*     */   {
/* 121 */     return lInf(this.smapVector, b.getSmapVector());
/*     */   }
/*     */ 
/*     */   private int pivotCount() {
/* 125 */     if (this.smapVector == null) {
/* 126 */       return 0;
/*     */     }
/* 128 */     return this.smapVector.length;
/*     */   }
/*     */ 
/*     */   public static int lInf(int[] smapVector, int[] other)
/*     */   {
/* 134 */     int cx = 0;
/* 135 */     int max = 0;
/*     */ 
/* 138 */     if ((smapVector == null) || (smapVector.length == 0)) {
/* 139 */       assert ((other == null) || (other.length == 0));
/* 140 */       return max;
/*     */     }
/*     */ 
/* 143 */     while (cx < smapVector.length) {
/* 144 */       int t = Math.abs(smapVector[cx] - other[cx]);
/* 145 */       if (t > max) {
/* 146 */         max = t;
/*     */       }
/* 148 */       ++cx;
/*     */     }
/* 150 */     return max;
/*     */   }
/*     */ 
/*     */   public static int[] convertTuple(OBInt object, OBInt[] pivots) throws OBException
/*     */   {
/* 155 */     int i = 0;
/* 156 */     int[] smapVector = new int[pivots.length];
/* 157 */     while (i < pivots.length) {
/* 158 */       int distance = pivots[i].distance(object);
/* 159 */       smapVector[i] = distance;
/* 160 */       ++i;
/*     */     }
/* 162 */     return smapVector;
/*     */   }
/*     */ 
/*     */   public int[] getSmapVector()
/*     */   {
/* 170 */     return this.smapVector;
/*     */   }
/*     */ 
/*     */   public void write(ByteBuffer out)
/*     */   {
/* 178 */     if (getSmapVector() != null) {
/* 179 */       for (int j : getSmapVector()) {
/* 180 */         out.putInt(j);
/*     */       }
/*     */     }
/* 183 */     out.putLong(getId());
/*     */   }
/*     */ 
/*     */   public void read(ByteBuffer in, int pivots)
/*     */   {
/* 192 */     if (pivots != 0) {
/* 193 */       this.smapVector = new int[pivots];
/* 194 */       int i = 0;
/* 195 */       while (i < pivots) {
/* 196 */         this.smapVector[i] = in.getInt();
/* 197 */         ++i;
/*     */       }
/*     */     }
/* 200 */     this.smapVector = null;
/* 201 */     super.setId(in.getLong());
/*     */   }
/*     */ 
/*     */   public void setSmapVector(int[] smapVector)
/*     */   {
/* 210 */     this.smapVector = smapVector;
/*     */   }
/*     */ 
/*     */   public int getPivotSize() {
/* 214 */     if (this.smapVector == null) {
/* 215 */       return 0;
/*     */     }
/* 217 */     return this.smapVector.length;
/*     */   }
/*     */ 
/*     */   public int compareTo(BucketObjectInt o)
/*     */   {
/* 226 */     int i = 0;
/* 227 */     assert (this.smapVector.length == o.smapVector.length);
/* 228 */     while (i < this.smapVector.length) {
/* 229 */       int res = compareDim(this.smapVector[i], o.smapVector[i]);
/* 230 */       if (res != 0) {
/* 231 */         return res;
/*     */       }
/* 233 */       ++i;
/*     */     }
/*     */ 
/* 236 */     return 0;
/*     */   }
/*     */ 
/*     */   private final int compareDim(int a, int b)
/*     */   {
/* 256 */     if (a < b)
/* 257 */       return -1;
/* 258 */     if (a > b) {
/* 259 */       return 1;
/*     */     }
/* 261 */     return 0;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 266 */     return Arrays.toString(this.smapVector);
/*     */   }
/*     */ }

/* Location:           C:\Software\obsearch\obsearch-0.9.9g-jar-with-dependencies.jar
 * Qualified Name:     net.obsearch.index.bucket.impl.BucketObjectInt
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.5.3
 */